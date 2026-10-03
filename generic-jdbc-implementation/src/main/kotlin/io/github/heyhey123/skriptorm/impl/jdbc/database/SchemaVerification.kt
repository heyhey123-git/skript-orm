package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import java.sql.Connection
import java.sql.DatabaseMetaData

/**
 * Checks whether an existing database table supports its registered declaration.
 *
 * Registration uses `CREATE TABLE IF NOT EXISTS`, which leaves an existing table unchanged.
 * If a script adds a column later, registration succeeds but writes to that column fail.
 * Verifying the schema immediately after registration reports the mismatch at its source.
 *
 * [DatabaseMetaData] provides a common path across MySQL, MariaDB, PostgreSQL, SQLite, and H2.
 * Type names shared across servers are normalized through [TYPE_ALIASES]. Server-specific names
 * belong in the dialect or, for the generic dialect, [PRODUCT_ALIASES].
 *
 * Every declared column must exist with compatible storage, and a declared key must identify a
 * single row. Extra columns are allowed, including those left by an older declaration or another
 * tool. Primary keys need a separate comparison; see [compare].
 *
 * Auto-increment and sizes reported in incompatible units are excluded. Drivers report identity
 * columns differently, and MySQL reports large-object `COLUMN_SIZE` in bytes; comparing either
 * directly would reject valid tables.
 *
 * A larger column can satisfy a smaller declared size. [JdbcDataType.servesSize] decides what
 * counts as sufficient capacity for each storage type, including fixed-width columns.
 */
internal object SchemaVerification {

    /**
     * Maximum number of table names suggested when a registered table cannot be found.
     */
    private const val CANDIDATE_LIMIT = 20

    /**
     * Column metadata used by the schema comparison.
     */
    data class ActualColumn(
        val name: String,
        val typeName: String,
        val nullable: Boolean,
        val size: Int?
    )

    /**
     * The server's columns in declaration order, plus its primary key columns.
     */
    data class ActualTable(
        val columns: List<ActualColumn>,
        val primaryKey: Set<String>
    )

    /**
     * Throws unless the table the server holds matches [declared].
     *
     * @param connection the connection used to register the table
     * @param declared the table the script registered
     * @param typeAliases dialect-specific type names from [JdbcDialect.typeAliases]
     * @throws IllegalArgumentException if the existing table does not support [declared]
     */
    fun requireMatches(
        connection: Connection,
        declared: Table,
        typeAliases: Map<String, String> = emptyMap()
    ) {
        val actual = read(connection, declared.name)
        val mismatches = compare(declared, actual, typeAliases, connection.metaData.databaseProductName)
        require(mismatches.isEmpty()) {
            val header = "Registered table '${declared.name}' does not match the table in the database. " +
                "Registration is 'CREATE TABLE IF NOT EXISTS', so a table that already exists is never " +
                "changed, and every statement that uses the column or type below will fail."
            val advice = "Drop the table and register it again, or change the table in the database to " +
                "match this declaration."
            (listOf(header) + mismatches + advice).joinToString("\n")
        }
    }

    /**
     * Returns missing columns first, then column mismatches, then primary key mismatches.
     * Extra columns do not prevent the table from serving the declaration.
     *
     * Names must match exactly because this implementation always quotes identifiers.
     *
     * [typeAliases] supplies dialect-specific names; [productName] selects server-specific aliases
     * for the generic dialect.
     */
    fun compare(
        declared: Table,
        actual: ActualTable,
        typeAliases: Map<String, String> = emptyMap(),
        productName: String? = null
    ): List<String> {
        val mismatches = mutableListOf<String>()
        val actualByName = actual.columns.associateBy { it.name }

        val missing = declared.columns.keys - actualByName.keys
        if (missing.isNotEmpty()) {
            mismatches += "Column(s) ${missing.joinToString(", ") { "'$it'" }} are declared but missing " +
                "from the table. The table holds: ${actual.columns.joinToString(", ") { "'${it.name}'" }}."
        }

        for ((name, column) in declared.columns) {
            val stored = actualByName[name] ?: continue
            val expectedType = typeNameOf(column)
            if (expectedType != null &&
                normalizeTypeName(expectedType, typeAliases, productName) !=
                normalizeTypeName(stored.typeName, typeAliases, productName)
            ) {
                mismatches += "Column '$name' is declared as $expectedType but the table holds " +
                    "${stored.typeName}."
            }
            val expectedSize = declaredSizeOf(column)
            val declaredType = column.type as? JdbcDataType<*>
            if (declaredType != null &&
                expectedSize != null &&
                stored.size != null &&
                !declaredType.servesSize(stored.size, expectedSize)
            ) {
                mismatches += "Column '$name' is declared with a size of $expectedSize but the table holds " +
                    "${stored.size}. A column of that size cannot serve the declaration."
            }
            // Primary keys are non-null on every supported server. Drivers report identity columns
            // differently, so only compare nullability on non-key columns.
            if (!column.isPrimaryKey && !column.isNullable && stored.nullable) {
                mismatches += "Column '$name' is declared 'not null' but the table allows null."
            }
        }

        // The declared key must uniquely identify rows in the existing table. An actual key using
        // undeclared columns cannot guarantee that. A keyless declaration makes no uniqueness claim.
        val declaredKey = declared.columns.filterValues { it.isPrimaryKey }.keys
        if (declaredKey.isNotEmpty()) {
            if (actual.primaryKey.isEmpty()) {
                mismatches += "The primary key is declared as ${declaredKey.describe()} but the table " +
                    "holds none."
            } else if (!declaredKey.containsAll(actual.primaryKey)) {
                mismatches += "The table keys on ${(actual.primaryKey - declaredKey).describe()}, which " +
                    "the declaration does not mark as a primary key, so a statement that addresses a row " +
                    "by ${declaredKey.describe()} can match more than one."
            }
        }

        return mismatches
    }

    /**
     * Reads the table the server holds.
     *
     * First locate the table in the active catalog or schema. MySQL and PostgreSQL use different
     * locations; reading columns without confirming the table could inspect the wrong one.
     */
    fun read(connection: Connection, table: String): ActualTable =
        readWith(connection.metaData, table)

    /**
     * Metadata-based form of [read], separated so table lookup can be unit tested. An unrestricted
     * MySQL catalog search once mistook `performance_schema.USER` for a registered `users` table.
     */
    fun readWith(metadata: DatabaseMetaData, table: String): ActualTable {
        val location = locate(metadata, metadata.connection.catalog, table)
        val columns = mutableListOf<ActualColumn>()
        metadata.getColumns(location.catalog, location.schema, table, null).use { rows ->
            while (rows.next()) {
                val name = rows.getString("COLUMN_NAME")
                val typeName = rows.getString("TYPE_NAME").orEmpty()
                val size = rows.getInt("COLUMN_SIZE")
                val hasSize = !rows.wasNull()
                columns += ActualColumn(
                    name = name,
                    typeName = typeName,
                    nullable = rows.getString("IS_NULLABLE").equals("YES", ignoreCase = true),
                    size = if (hasSize) size else null
                )
            }
        }

        val primaryKey = linkedSetOf<String>()
        metadata.getPrimaryKeys(location.catalog, location.schema, table).use { rows ->
            while (rows.next()) primaryKey += rows.getString("COLUMN_NAME")
        }

        return ActualTable(columns, primaryKey)
    }

    /**
     * Finds [table] in the connection's catalog or schema and returns the location needed for metadata
     * queries. Restricting the catalog prevents a similarly named table in MySQL's
     * `performance_schema` from being mistaken for the registered table.
     *
     * Matches the reported name exactly first, then without regard to case. Some MySQL setups store
     * table names in a different case from the lookup pattern. If no table matches, registration fails
     * with a list of candidate names from the active catalog.
     *
     * @throws IllegalArgumentException when no table with that name exists where the connection can see it
     */
    private fun locate(metadata: DatabaseMetaData, catalog: String?, table: String): Location {
        val found = linkedMapOf<String, Location>()
        metadata.getTables(catalog, null, table, null).use { rows ->
            while (rows.next()) {
                val name = rows.getString("TABLE_NAME") ?: continue
                found[name] = Location(rows.getString("TABLE_CAT"), rows.getString("TABLE_SCHEM"))
            }
        }
        found[table]?.let { return it }
        found.entries
            .firstOrNull { it.key.equals(table, ignoreCase = true) }
            ?.let { return it.value }

        // List names in the active catalog to handle drivers whose lookup pattern is case-sensitive.
        // Cap the list to keep an error from dumping the entire catalog.
        val elsewhere = mutableListOf<String>()
        if (catalog != null) {
            metadata.getTables(catalog, null, null, null).use { rows ->
                while (rows.next() && elsewhere.size <= CANDIDATE_LIMIT) {
                    rows.getString("TABLE_NAME")?.let { elsewhere += it }
                }
            }
        }
        elsewhere.firstOrNull { it == table }?.let { return Location(catalog, null) }
        elsewhere.firstOrNull { it.equals(table, ignoreCase = true) }
            ?.let { return Location(catalog, null) }

        throw IllegalArgumentException(cannotLocate(table, catalog, elsewhere))
    }

    private fun cannotLocate(table: String, catalog: String?, held: List<String>): String {
        val where = if (catalog != null) "database '$catalog'" else "the connected database"
        val listing = when {
            held.isEmpty() -> "It holds no tables."
            held.size > CANDIDATE_LIMIT ->
                "It holds more than $CANDIDATE_LIMIT tables; this one is not among the first " +
                    "$CANDIDATE_LIMIT."
            else -> "It holds: ${held.joinToString(", ") { "'$it'" }}."
        }
        return "Registered table '$table' does not exist in $where, so there is nothing to compare the " +
            "declaration with. $listing Registration is 'CREATE TABLE IF NOT EXISTS', which is a no-op " +
            "when the table exists and was expected to create it here; the table name, the database the " +
            "connection selected, or the privileges of its user are what to check."
    }

    /**
     * Catalog and schema used to look up a table through JDBC metadata. Either may be absent,
     * depending on the database.
     */
    data class Location(val catalog: String?, val schema: String?)

    /**
     * Returns the JDBC storage type declared for [column], or null if it has no JDBC type.
     */
    private fun typeNameOf(column: Column<*>): String? {
        val type = column.type as? JdbcDataType<*> ?: return null
        return type.storageName
    }

    /** The size the dialect would write: the declared one, else the type's own default. */
    private fun declaredSizeOf(column: Column<*>): Int? {
        val type = column.type as? JdbcDataType<*> ?: return null
        return column.size ?: type.defaultSize.takeIf { it >= 0 }
    }

    /**
     * Normalizes a type name for comparison: removes its size, converts it to uppercase, then checks
     * dialect-specific, product-specific, and shared aliases in that order. Unknown names remain
     * unchanged so different storage types still produce a mismatch.
     */
    fun normalizeTypeName(
        name: String,
        typeAliases: Map<String, String> = emptyMap(),
        productName: String? = null
    ): String {
        val bare = name.substringBefore('(').trim().uppercase()
        val byProduct = productName?.uppercase()?.let { PRODUCT_ALIASES[it] }
        return typeAliases[bare] ?: byProduct?.get(bare) ?: TYPE_ALIASES[bare] ?: bare
    }

    /**
     * Type names with the same meaning across supported databases, including standard SQL spellings
     * such as `CHARACTER VARYING`, `BINARY VARYING`, and `DOUBLE PRECISION`.
     *
     * Names whose meaning depends on the database belong in its dialect or in [PRODUCT_ALIASES].
     * For example, `INT8` is not a portable spelling for a big integer, and `LONGVARCHAR` has
     * driver-specific handling.
     */
    private val TYPE_ALIASES: Map<String, String> = mapOf(
        "INT" to "INTEGER",
        "DOUBLE PRECISION" to "DOUBLE",
        "TEXT" to "VARCHAR",
        "CHARACTER VARYING" to "VARCHAR",
        "BINARY VARYING" to "VARBINARY",
        "CHARACTER LARGE OBJECT" to "BLOB",
        "BINARY LARGE OBJECT" to "BLOB"
    )

    /**
     * Aliases that apply only to a named database product when using the generic JDBC dialect.
     * H2 reports a declared `FLOAT` column as `DOUBLE PRECISION`; mapping `FLOAT` to `DOUBLE`
     * lets schema verification recognize the table the addon created. H2's `REAL` remains a
     * distinct four-byte floating-point type.
     */
    private val PRODUCT_ALIASES: Map<String, Map<String, String>> = mapOf(
        "H2" to mapOf("FLOAT" to "DOUBLE")
    )

    private fun Set<String>.describe(): String =
        if (isEmpty()) "none" else joinToString(", ") { "'$it'" }
}
