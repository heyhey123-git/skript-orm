package io.github.heyhey123.skriptorm.impl.jdbc.integration

import io.github.heyhey123.skriptorm.database.ConnectionSettings
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDatabase
import io.github.heyhey123.skriptorm.impl.jdbc.database.MysqlJdbcDialect
import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.LocationJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import kotlinx.coroutines.runBlocking
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Checks that the dialect's DDL creates the declared schema on a real server. Registration also
 * compares the declaration with the server's reported column types, catching incompatible aliases.
 */
abstract class SchemaIntegrationTest : MysqlIntegrationTestBase() {

    @Test
    fun `registerTable creates every declared column`() = runBlocking<Unit> {
        recreateTable()

        val columns = columnMetadata("users")
        assertEquals(usersTable.columns.keys, columns.map { it.name }.toSet())
        assertEquals(usersTable.columns.size, columns.size)
    }

    @Test
    fun `declared column types and sizes survive the round trip to the server`() = runBlocking<Unit> {
        recreateTable()
        val columns = columnMetadata("users").associateBy { it.name }

        assertColumnType("name", "VARCHAR", columns)
        assertColumnSize("name", 100, columns)
        assertColumnType("id", "INT", columns)
        assertColumnType("age", "INT", columns)
        assertColumnType("big", "BIGINT", columns)
        assertColumnType("tiny", "TINYINT", columns)
        assertColumnType("score", "DOUBLE", columns)
        assertColumnType("ratio", "FLOAT", columns)
        assertColumnType("uid", "BINARY", columns)
        assertColumnSize("uid", 16, columns)

        // BOOLEAN uses TINYINT storage here. Drivers may report the column as BIT, BOOLEAN, or
        // TINYINT; TypeRoundTripIntegrationTest checks the value itself.
        assertTrue(
            columns.getValue("active").typeName in setOf("BOOLEAN", "TINYINT", "BIT"),
            "active was reported as ${columns.getValue("active")}"
        )
    }

    /**
     * With `tinyInt1isBit=false`, the driver reports a declared boolean column as `TINYINT`.
     * Registration must accept that metadata name for the same underlying storage.
     */
    @Test
    fun `a boolean column is accepted over a connection that reports it as a tinyint`() = runBlocking<Unit> {
        recreateTable()

        val separator = if (dataSource.jdbcUrl.contains('?')) "&" else "?"
        val url = dataSource.jdbcUrl + separator + "tinyInt1isBit=false"
        assertEquals("TINYINT", reportedTypeName(url, "users", "active"))

        val flagged = JdbcDatabase(dataSource.driverClassName, MysqlJdbcDialect)
        Database.connectNamed(
            "tinyInt1isBit=false",
            flagged,
            ConnectionSettings(url, dataSource.username, dataSource.password)
        )

        flagged.registerTable(usersTable)
    }

    /** The type name a driver reports for one column, read outside the implementation. */
    private fun reportedTypeName(url: String, table: String, column: String): String? {
        DriverManager.getConnection(url, dataSource.username, dataSource.password).use { connection ->
            connection.metaData.getColumns(connection.catalog, null, table, null).use { rows ->
                while (rows.next()) {
                    if (rows.getString("COLUMN_NAME") == column) return rows.getString("TYPE_NAME")
                }
            }
        }
        return null
    }

    /** Includes the full column metadata in assertion failures. */
    private fun assertColumnType(column: String, expected: String, columns: Map<String, ColumnMeta>) =
        assertEquals(expected, columns.getValue(column).typeName, "type of $column: ${columns.getValue(column)}")

    private fun assertColumnSize(column: String, expected: Int, columns: Map<String, ColumnMeta>) =
        assertEquals(expected, columns.getValue(column).size, "size of $column: ${columns.getValue(column)}")

    @Test
    fun `nullability follows the column declaration`() = runBlocking<Unit> {
        recreateTable()
        val columns = columnMetadata("users").associateBy { it.name }

        assertFalse(columns.getValue("id").nullable, "the primary key was declared NOT NULL")
        assertFalse(columns.getValue("name").nullable, "name was declared NOT NULL")
        assertTrue(columns.getValue("age").nullable, "age was left nullable")
        assertTrue(columns.getValue("uid").nullable, "uid was left nullable")
    }

    @Test
    fun `the declared primary key is the one the server enforces`() = runBlocking<Unit> {
        recreateTable()
        assertEquals(listOf("id"), primaryKeys("users"))
    }

    @Test
    fun `not null columns reject sql null`() = runBlocking<Unit> {
        recreateTable()

        assertFailsWith<SQLException> {
            queries.insertOne(linkedMapOf<String, Any?>("name" to null)).execute(usersTable)
        }
        assertEquals(0L, rawRowCount())
    }

    @Test
    fun `the primary key rejects duplicates`() = runBlocking<Unit> {
        recreateTable()
        queries.insertOne(userValues(id = 1, name = "first")).execute(usersTable)

        assertFailsWith<SQLException> {
            queries.insertOne(userValues(id = 1, name = "second")).execute(usersTable)
        }
        assertEquals(listOf("first"), allUsers().map { it.name })
    }

    @Test
    fun `auto increment generates sequential identifiers`() = runBlocking<Unit> {
        recreateTable()

        queries.insertOne(userValues(name = "first")).execute(usersTable)
        queries.insertOne(userValues(name = "second")).execute(usersTable)

        assertEquals(listOf(1, 2), allUsers().map { it.id })
    }

    @Test
    fun `registering the same table again keeps existing rows`() = runBlocking<Unit> {
        recreateTable()
        queries.insertOne(userValues(id = 7, name = "kept")).execute(usersTable)

        database.registerTable(usersTable)

        assertEquals(1L, rawRowCount())
        assertEquals(listOf("kept"), allUsers().map { it.name })
    }

    /**
     * An existing table is not changed by `CREATE TABLE IF NOT EXISTS`. Registration must report
     * the missing `age` column before an insert tries to use it.
     */
    @Test
    fun `a declaration the existing table does not match is refused at registration`() = runBlocking<Unit> {
        executeSql("DROP TABLE IF EXISTS `older_users`")
        executeSql("CREATE TABLE `older_users` (`id` INT NOT NULL PRIMARY KEY, `name` VARCHAR(64) NOT NULL)")
        val withAge = Table(
            "older_users",
            listOf(
                Column("id", IntJdbcDataType(), isPrimaryKey = true, isNullable = false),
                Column("name", StringJdbcDataType(), isNullable = false, size = 64),
                Column("age", IntJdbcDataType())
            )
        )

        val thrown = assertFailsWith<IllegalArgumentException> { database.registerTable(withAge) }

        assertTrue("'age'" in thrown.message.orEmpty(), thrown.message.orEmpty())
        assertTrue("'id', 'name'" in thrown.message.orEmpty(), thrown.message.orEmpty())
    }

    /**
     * An existing `TEXT` column can hold the values declared for a 255-character `string` column,
     * so registration should accept it.
     */
    @Test
    fun `a column wider than the declaration asks for is registered`() = runBlocking<Unit> {
        executeSql("DROP TABLE IF EXISTS `wide_users`")
        executeSql("CREATE TABLE `wide_users` (`id` INT NOT NULL PRIMARY KEY, `name` TEXT NOT NULL)")
        val declared = Table(
            "wide_users",
            listOf(
                Column("id", IntJdbcDataType(), isPrimaryKey = true, isNullable = false),
                Column("name", StringJdbcDataType(), isNullable = false)
            )
        )

        database.registerTable(declared)

        queries.insertOne(linkedMapOf<String, Any?>("id" to 1, "name" to "wider")).execute(declared)
        assertEquals(1L, rawRowCount("wide_users"))
    }

    /**
     * A wider `VARBINARY` column can hold the serialized `location` value and should pass
     * schema verification.
     */
    @Test
    fun `a wider byte column is registered for a location`() = runBlocking<Unit> {
        executeSql("DROP TABLE IF EXISTS `wide_places`")
        executeSql("CREATE TABLE `wide_places` (`id` INT NOT NULL PRIMARY KEY, `spot` VARBINARY(8192) NOT NULL)")
        val declared = Table(
            "wide_places",
            listOf(
                Column("id", IntJdbcDataType(), isPrimaryKey = true, isNullable = false),
                Column("spot", LocationJdbcDataType(), isNullable = false)
            )
        )

        database.registerTable(declared)
    }

    @Test
    fun `reserved words are quoted and usable as identifiers`() = runBlocking<Unit> {
        val reserved = Table(
            "order",
            listOf(
                Column("select", IntJdbcDataType(), isPrimaryKey = true, isNullable = false),
                Column("from", StringJdbcDataType(), isNullable = false, size = 20)
            )
        )
        recreateTable(reserved)

        queries.insertOne(linkedMapOf<String, Any?>("select" to 1, "from" to "value")).execute(reserved)

        assertEquals(1L, rawRowCount("order"))
        assertEquals(
            listOf("value"),
            queries.selectMany(null).execute(reserved).readColumn("from", StringJdbcDataType())
        )
    }

    @Test
    fun `unicode identifiers are quoted and usable`() = runBlocking<Unit> {
        val unicode = Table(
            "用户",
            listOf(
                Column("编号", IntJdbcDataType(), isPrimaryKey = true, isNullable = false),
                Column("名前", StringJdbcDataType(), isNullable = false, size = 50)
            )
        )
        recreateTable(unicode)

        queries.insertOne(linkedMapOf<String, Any?>("编号" to 1, "名前" to "值")).execute(unicode)

        assertEquals(1L, rawRowCount("用户"))
        assertEquals(
            listOf("值"),
            queries.selectMany(null).execute(unicode).readColumn("名前", StringJdbcDataType())
        )
    }

    @Test
    fun `a table without a primary key is still created`() = runBlocking<Unit> {
        val keyless = Table("keyless", listOf(Column("value", IntJdbcDataType())))
        recreateTable(keyless)

        assertTrue(primaryKeys("keyless").isEmpty())
        queries.insertOne(linkedMapOf<String, Any?>("value" to 5)).execute(keyless)
        assertEquals(1L, rawRowCount("keyless"))
    }
}
