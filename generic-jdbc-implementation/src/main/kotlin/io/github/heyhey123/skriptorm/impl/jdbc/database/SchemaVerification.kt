package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import java.sql.Connection
import java.sql.DatabaseMetaData

/**
 * Compares a registered table against the table the server actually holds, and says what differs.
 *
 * Registration is `CREATE TABLE IF NOT EXISTS`, which is deliberately a no-op when the table is already
 * there: registering the same table on every startup has to be safe, and the row-keeping integration test
 * depends on it. What that statement cannot do is notice that the declaration moved on. A column added to
 * a script never reaches a table that already exists, and the first statement to mention it is refused by
 * the server — `Unknown column 'age' in 'INSERT INTO'` — which names the insert rather than the
 * registration, so the script reads as if the write were wrong.
 *
 * The check therefore runs straight after the DDL, while the table is known to exist, and reports the
 * declaration and the reality side by side. It reads [DatabaseMetaData] rather than asking each server for
 * its own `information_schema`, so MySQL, MariaDB, PostgreSQL, SQLite and H2 are all covered by the same
 * path — the same path the integration tests already use to read a column back.
 *
 * What it does not check is auto-increment, and any size a server reports in units other than the one the
 * declaration used. The drivers disagree about both in ways that are not about the script at all: MySQL
 * reports an identity column through `IS_AUTOINCREMENT` while PostgreSQL reports the underlying sequence,
 * and MySQL's `COLUMN_SIZE` for a large-object column is a byte count rather than the declared size. A
 * comparison that failed on those would refuse correct tables, which is worse than the silence it replaces.
 */
internal object SchemaVerification {

    /**
     * What the server reports for one column, in the terms this comparison needs.
     */
    data class ActualColumn(
        val name: String,
        val typeName: String,
        val nullable: Boolean,
        val size: Int?
    )

    /**
     * A table as the server holds it: its columns in declaration order, and the columns of its primary key.
     */
    data class ActualTable(
        val columns: List<ActualColumn>,
        val primaryKey: Set<String>
    )

    /**
     * Throws unless the table the server holds matches [declared].
     *
     * @param connection a connection with the schema in effect, which is the one the DDL just ran on
     * @param declared the table the script registered
     * @throws IllegalArgumentException naming every difference, or doing nothing when there is none
     */
    fun requireMatches(connection: Connection, declared: Table) {
        val actual = read(connection, declared.name)
        val mismatches = compare(declared, actual)
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
     * Every difference between [declared] and [actual], in the order a reader wants them: a column that is
     * missing altogether first, then one the declaration does not know, then the per-column differences.
     *
     * The comparison is exact for names. A server that folds unquoted identifiers to lower case does not
     * hide a difference, because this implementation always quotes them: what a script writes is the name
     * the server holds, or the table is not the one it registered.
     */
    fun compare(declared: Table, actual: ActualTable): List<String> {
        val mismatches = mutableListOf<String>()
        val actualByName = actual.columns.associateBy { it.name }

        val missing = declared.columns.keys - actualByName.keys
        if (missing.isNotEmpty()) {
            mismatches += "Column(s) ${missing.joinToString(", ") { "'$it'" }} are declared but missing " +
                "from the table. The table holds: ${actual.columns.joinToString(", ") { "'${it.name}'" }}."
        }

        val unexpected = actual.columns.map { it.name } - declared.columns.keys
        if (unexpected.isNotEmpty()) {
            mismatches += "Column(s) ${unexpected.joinToString(", ") { "'$it'" }} are in the table but not " +
                "declared, so the declaration is behind the table."
        }

        for ((name, column) in declared.columns) {
            val stored = actualByName[name] ?: continue
            val expectedType = typeNameOf(column)
            if (expectedType != null && normalizeTypeName(expectedType) != normalizeTypeName(stored.typeName)) {
                mismatches += "Column '$name' is declared as $expectedType but the table holds " +
                    "${stored.typeName}."
            }
            val expectedSize = declaredSizeOf(column)
            if (expectedSize != null && stored.size != null && stored.size != expectedSize) {
                mismatches += "Column '$name' is declared with a size of $expectedSize but the table " +
                    "holds ${stored.size}."
            }
            // A primary key is not null on every server this runs on, whatever the declaration says, and
            // the drivers disagree about which of the two they report for an identity column. Only a
            // non-key column is compared, where "not null" is what the script asked for and nothing else.
            if (!column.isPrimaryKey && !column.isNullable && stored.nullable) {
                mismatches += "Column '$name' is declared 'not null' but the table allows null."
            }
        }

        val declaredKey = declared.columns.filterValues { it.isPrimaryKey }.keys
        if (declaredKey != actual.primaryKey) {
            mismatches += "The primary key is declared as " +
                "${declaredKey.describe()} but the table holds ${actual.primaryKey.describe()}."
        }

        return mismatches
    }

    /**
     * Reads the table the server holds.
     *
     * The table is looked up before its columns are, because a table lives in a catalog on MySQL and in a
     * schema on PostgreSQL, and the same call has to find it on both. A table the lookup cannot find is
     * reported as one with no columns, which makes every declared column a difference: a table that is not
     * there is the largest mismatch of all.
     */
    fun read(connection: Connection, table: String): ActualTable =
        readWith(connection.metaData, table)

    /**
     * [read], taking the metadata rather than a connection.
     *
     * The two are separate so that the part a server can get wrong — where the table is, and which one was
     * found — is unit-testable. That part has been wrong once: the catalogue search ran with no catalogue
     * pattern, which on MySQL searches every schema it can see, so `performance_schema.USER` was taken for
     * the registered `users`.
     */
    fun readWith(metadata: DatabaseMetaData, table: String): ActualTable {
        val catalog = metadata.connection.catalog
        val location = locate(metadata, catalog, table)
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
     * Where [table] lives, as the catalog and schema a metadata read has to be given.
     *
     * Every lookup stays inside the connection's own catalog. [DatabaseMetaData.getTables] with a null
     * catalog searches every schema the account can see, and MySQL installs one called `performance_schema`
     * that holds a table for each instrument; a declared `users` matched its `USER`, and the comparison then
     * reported every declared column missing from a table the script had never named. A registration only
     * ever concerns the table the connection is already using, so a table found anywhere else is not the one
     * that was registered and must not be compared with it.
     *
     * Within that catalog the name is matched as the server reports it, and only then without case: MySQL on
     * a case-sensitive filesystem compares the pattern as written and stores what the DDL wrote, so asking
     * for `users` can miss a table the same server hands over as `USERS`.
     *
     * The location comes from the server's own answer rather than from [Connection.getCatalog] alone,
     * because PostgreSQL answers that with null: it has no catalogs, and its table is found by schema.
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

        val connection = metadata.connection
        return Location(catalog ?: connection.catalog, connection.schema)
    }

    /**
     * Where a table is: the catalog and schema a metadata read has to be given.
     *
     * Both are nullable because no server has both: MySQL has a catalog and no schema, PostgreSQL the other
     * way round, and SQLite neither.
     */
    data class Location(val catalog: String?, val schema: String?)

    /**
     * The type name the dialect writes for [column], or null when it is not a type this implementation
     * renders. A declaration reached this far has one, so null means only that the comparison has nothing
     * to say about that column.
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
     * The comparable form of a type name: upper case, without a `(size)`, through [TYPE_ALIASES].
     *
     * The two servers spell the same storage differently — `INT8` against `BIGINT`, `VARCHAR` against
     * `CHARACTER VARYING` — and the aliases are the complete set of ways they spell the types this
     * implementation renders. A name that is not listed is compared as it stands, so a genuinely different
     * type still differs.
     */
    fun normalizeTypeName(name: String): String {
        val bare = name.substringBefore('(').trim().uppercase()
        return TYPE_ALIASES[bare] ?: bare
    }

    /**
     * Every spelling the servers use for the storage this implementation writes, mapped to the name the
     * dialect writes.
     *
     * PostgreSQL's driver reports its own internal names (`int4`, `int8`, `float8`) through
     * `DatabaseMetaData`, while MySQL reports the SQL name and H2 reports the standard one. The canonical
     * form is not a preference: it is the spelling the declaration uses, so that the two can be compared
     * at all.
     */
    private val TYPE_ALIASES: Map<String, String> = mapOf(
        "INT" to "INTEGER",
        "INT4" to "INTEGER",
        "INT8" to "BIGINT",
        "INT2" to "SMALLINT",
        "SERIAL" to "INTEGER",
        "BIGSERIAL" to "BIGINT",
        "SMALLSERIAL" to "SMALLINT",
        "FLOAT4" to "REAL",
        "FLOAT8" to "DOUBLE PRECISION",
        "BOOL" to "BOOLEAN",
        // MySQL has no boolean of its own: `BOOLEAN` is `TINYINT(1)`, and Connector/J reports the column
        // it created as `BIT` — one bit, which is the same one-byte column. Neither name can be declared
        // here except as `boolean`, so both are that type.
        "BIT" to "BOOLEAN",
        "CHARACTER VARYING" to "VARCHAR",
        "CHARACTER LARGE OBJECT" to "BLOB",
        "BINARY LARGE OBJECT" to "BLOB",
        "BYTEA" to "BLOB",
        "LONGVARBINARY" to "BLOB",
        "LONGVARCHAR" to "BLOB",
        "TINYBLOB" to "BLOB",
        "MEDIUMBLOB" to "BLOB",
        "LONGBLOB" to "BLOB",
        "TINYTEXT" to "VARCHAR",
        "MEDIUMTEXT" to "VARCHAR",
        "LONGTEXT" to "VARCHAR",
        "TEXT" to "VARCHAR"
    )

    private fun Set<String>.describe(): String =
        if (isEmpty()) "none" else joinToString(", ") { "'$it'" }
}
