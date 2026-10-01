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
 * What it requires is that the table can serve every statement the declaration allows: every declared column
 * is there, with the storage the declaration asked for, and the declared key names one row. What it does not
 * require is that the table holds nothing else. A table this plugin shares with another tool, or one an
 * older declaration of the same script created with columns this one no longer names, still answers every
 * statement the declaration can make, so that direction is not a difference. The key is the one part checked
 * the other way round, because there a wider declaration is not the safe side: see [compare].
 *
 * What it does not check is auto-increment, and any size a server reports in units other than the one the
 * declaration used. The drivers disagree about both in ways that are not about the script at all: MySQL
 * reports an identity column through `IS_AUTOINCREMENT` while PostgreSQL reports the underlying sequence,
 * and MySQL's `COLUMN_SIZE` for a large-object column is a byte count rather than the declared size. A
 * comparison that failed on those would refuse correct tables, which is worse than the silence it replaces.
 */
internal object SchemaVerification {

    /**
     * How many table names a "this table does not exist" message may name.
     *
     * Enough to recognise a mistaken name or a mistaken database, few enough that a script's mistake does
     * not print a catalogue into the console.
     */
    private const val CANDIDATE_LIMIT = 20

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
     * @param typeAliases the spellings the connection's own dialect reports for a storage, from
     *   [JdbcDialect.typeAliases]. They have to come from the dialect because the same spelling can be two
     *   different storages on two servers, so the comparison cannot carry them itself.
     * @throws IllegalArgumentException naming every difference, or doing nothing when there is none
     */
    fun requireMatches(
        connection: Connection,
        declared: Table,
        typeAliases: Map<String, String> = emptyMap()
    ) {
        val actual = read(connection, declared.name)
        val mismatches = compare(declared, actual, typeAliases)
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
     * Every difference that would stop the table from serving the declaration, in the order a reader wants
     * them: a column that is missing altogether first, then the per-column differences, then the key.
     *
     * A column the table has and the declaration does not is not one of them. What a declaration asks for is
     * that the columns it names are there with the storage it asked for; it asks for nothing else, and a
     * table that carries more than that still runs every statement the script can write.
     *
     * The comparison is exact for names. A server that folds unquoted identifiers to lower case does not
     * hide a difference, because this implementation always quotes them: what a script writes is the name
     * the server holds, or the table is not the one it registered.
     *
     * [typeAliases] comes from the connection's dialect and names the spellings that dialect's driver
     * reports for a storage this implementation writes; see [JdbcDialect.typeAliases].
     */
    fun compare(
        declared: Table,
        actual: ActualTable,
        typeAliases: Map<String, String> = emptyMap()
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
                normalizeTypeName(expectedType, typeAliases) != normalizeTypeName(stored.typeName, typeAliases)
            ) {
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

        // A declaration says that its key names one row, so the table has to be able to guarantee it: a
        // primary key whose columns are all key columns of the declaration does, because a superset of a
        // unique set is still unique. A table keyed on a column the declaration leaves out does not, because
        // a statement addressing a row by the declared key would match every row of that other key, and
        // `update one` would write all of them. The check is written as containment because that is the
        // requirement, even though a declaration carries at most one key column — `Table` refuses a second,
        // so the accepted direction cannot arise yet. A keyless declaration promises nothing about identity
        // and is not compared, which is the case this relaxation is about.
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
     * The table is looked up before its columns are, because a table lives in a catalog on MySQL and in a
     * schema on PostgreSQL, and the same call has to find it on both. A table the lookup cannot find is not
     * read: a comparison against a location nobody confirmed would report whatever that location happens to
     * hold, which is a wrong answer rather than a missing one.
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
     * A table that cannot be found ends the registration here. There is deliberately no fallback to the
     * connection's own catalog and schema: that is a guess, and a guess that then reads *some* table's
     * columns makes a refusal look like a column difference in a table that was never identified. What the
     * server does hold is named instead, so the reason is not left to be discovered.
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

        // Asked for by catalogue instead of by name, which is what finds a table the driver will not match
        // a differently-cased pattern against. The result is capped: a script's mistake must not print a
        // catalogue's contents into the console.
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
     * The comparable form of a type name: upper case, without a `(size)`, through the aliases.
     *
     * The servers spell the same storage differently — `INT8` against `BIGINT`, `VARCHAR` against
     * `CHARACTER VARYING` — and the aliases are the complete set of ways they spell the types this
     * implementation renders. A name that is not listed is compared as it stands, so a genuinely different
     * type still differs.
     *
     * [typeAliases] is the connection's dialect, which knows the spellings that are its own; see
     * [JdbcDialect.typeAliases]. It is consulted first, because a dialect that maps a name is saying
     * something about its own server.
     */
    fun normalizeTypeName(name: String, typeAliases: Map<String, String> = emptyMap()): String {
        val bare = name.substringBefore('(').trim().uppercase()
        return typeAliases[bare] ?: TYPE_ALIASES[bare] ?: bare
    }

    /**
     * Every spelling the standard and the JDBC drivers use for the storage this implementation writes, mapped
     * to the name this implementation declares.
     *
     * This is the part of the alias knowledge that belongs to no product: SQLite reports what the declaration
     * wrote, H2 reports the standard SQL name, and a JDBC driver may report either. What one product's driver
     * decided on its own goes in that product's dialect instead, because the same spelling can be two
     * different storages on two servers — `TINYINT` is `BOOLEAN` on MySQL and a type of its own on H2.
     * PostgreSQL's internal names (`int4`, `int8`, `bytea`) belong to the PostgreSQL dialect for the same
     * reason, and are declared there.
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
