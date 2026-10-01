package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.impl.jdbc.type.BigIntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.BooleanJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.DoubleJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.FloatJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.TinyIntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.UuidJdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What H2 says about the columns this dialect writes, asked of a server rather than assumed.
 *
 * H2 is the one server the documentation names that nothing else here reaches: the JDBC integration tests
 * need a container, and the Skript server test runs SQLite. The comparison registration runs is new in this
 * release, so whether it accepts a table H2 already holds was open, and H2 keeps a column's declared
 * spelling beside a canonical one — only its own driver can say which of the two comes back.
 *
 * Measured: H2 reports `INTEGER` for an `INT`, `CHARACTER VARYING` for a `VARCHAR`, `BINARY VARYING` for a
 * `VARBINARY` and `BINARY LARGE OBJECT` for a `BLOB`, all of which the shared table already answers. It also
 * reports `DOUBLE PRECISION` for a `DOUBLE` and — because H2 has no four-byte float under that name — for a
 * `FLOAT`, which is why `DOUBLE PRECISION` is a shared spelling and `FLOAT` is filed under H2's own product
 * name in [SchemaVerification]. These tests are what keep both honest.
 *
 * Nothing here needs a container, and the storages whose types would need Bukkit are made by hand. That is
 * why this can live in the plain unit tests, whose classpath the build keeps free of Bukkit and Skript.
 */
class H2SchemaTest {

    /**
     * The plugin's own declaration, laid down by the plugin's own DDL and then read back through the same
     * comparison registration runs. This is the whole question in one test: if H2 reports a canonical name
     * where the declaration wrote another, the mismatch shows up here and nowhere else.
     */
    @Test
    fun `a table this dialect creates is accepted when it is read back`() {
        open().use { connection ->
            // The product name is the key the aliases are filed under, so it is pinned here: a server that
            // called itself something else would silently stop being answered with its own spellings.
            assertEquals("H2", connection.metaData.databaseProductName)

            val declared = Table(
                "h2_users",
                listOf(
                    Column("id", BigIntJdbcDataType(), isPrimaryKey = true, isNullable = false),
                    Column("tiny", TinyIntJdbcDataType(), isNullable = false),
                    Column("age", IntJdbcDataType(), isNullable = false),
                    Column("active", BooleanJdbcDataType(), isNullable = false),
                    Column("ratio", DoubleJdbcDataType(), isNullable = false),
                    Column("share", FloatJdbcDataType(), isNullable = false),
                    Column("name", StringJdbcDataType(), isNullable = false),
                    Column("uid", UuidJdbcDataType(), isNullable = false)
                )
            )

            connection.createStatement().use { it.execute("DROP TABLE IF EXISTS \"h2_users\"") }
            connection.createStatement().use { it.execute(GenericJdbcDialect.createTable(declared)) }
            report(connection, "h2_users")

            SchemaVerification.requireMatches(connection, declared, GenericJdbcDialect.typeAliases)
        }
    }

    /**
     * The two byte storages whose types need Bukkit, so they are declared by hand and only what H2 reports
     * for them is checked: a name the generic dialect does not answer with its storage would mean a table
     * that holds either could be refused for a declaration that writes both.
     */
    @Test
    fun `the names H2 reports for byte columns are the storages the dialect declares`() {
        open().use { connection ->
            connection.createStatement().use { it.execute("DROP TABLE IF EXISTS \"h2_binary\"") }
            connection.createStatement().use {
                it.execute("CREATE TABLE \"h2_binary\" (\"spot\" VARBINARY(2048) NOT NULL, \"stack\" BLOB NOT NULL)")
            }
            report(connection, "h2_binary")

            val reported = names(connection, "h2_binary")
            assertEquals(
                "VARBINARY",
                SchemaVerification.normalizeTypeName(reported.getValue("spot"), GenericJdbcDialect.typeAliases)
            )
            assertEquals(
                "BLOB",
                SchemaVerification.normalizeTypeName(reported.getValue("stack"), GenericJdbcDialect.typeAliases)
            )
        }
    }

    /**
     * A column H2 declares with a spelling the plugin never writes, read back through the dialect. What the
     * comparison normalizes has to be the same storage on both sides, or a table made elsewhere is refused
     * for a declaration that would run every statement against it.
     */
    @Test
    fun `a text column H2 names its own way is still the stored string`() {
        open().use { connection ->
            connection.createStatement().use { it.execute("DROP TABLE IF EXISTS \"h2_legacy\"") }
            connection.createStatement().use {
                it.execute(
                    "CREATE TABLE \"h2_legacy\" (" +
                        "\"id\" BIGINT NOT NULL PRIMARY KEY, " +
                        "\"name\" CHARACTER VARYING(255) NOT NULL, " +
                        "\"score\" DOUBLE PRECISION NOT NULL)"
                )
            }
            report(connection, "h2_legacy")

            val declared = Table(
                "h2_legacy",
                listOf(
                    Column("id", BigIntJdbcDataType(), isPrimaryKey = true, isNullable = false),
                    Column("name", StringJdbcDataType(), isNullable = false),
                    Column("score", DoubleJdbcDataType(), isNullable = false)
                )
            )

            SchemaVerification.requireMatches(connection, declared, GenericJdbcDialect.typeAliases)
        }
    }

    private companion object {

        /**
         * One in-memory database for the JVM, kept open between connections. Tables are dropped before they
         * are made, so a rerun asks the same question as the first run.
         */
        fun open(): Connection = DriverManager.getConnection("jdbc:h2:mem:skriptorm;DB_CLOSE_DELAY=-1", "sa", "")

        /** What the server reports for each column of [table], keyed by the name it is stored under. */
        fun names(connection: Connection, table: String): Map<String, String> {
            val reported = linkedMapOf<String, String>()
            connection.metaData.getColumns(null, null, table, null).use { rows ->
                while (rows.next()) {
                    reported[rows.getString("COLUMN_NAME").lowercase()] = rows.getString("TYPE_NAME")
                }
            }
            assertTrue(reported.isNotEmpty(), "the server reported no columns for '$table'")
            return reported
        }

        /**
         * Prints what the server reports, the way the integration tests print what the drivers say. The
         * assertions above are what the test is for; this is what a failure needs to be readable.
         */
        fun report(connection: Connection, table: String) {
            connection.metaData.getColumns(null, null, table, null).use { rows ->
                while (rows.next()) {
                    println(
                        "[h2 probe] $table ${rows.getString("COLUMN_NAME")} -> " +
                            "${rows.getString("TYPE_NAME")}(${rows.getInt("COLUMN_SIZE")})"
                    )
                }
            }
        }
    }
}
