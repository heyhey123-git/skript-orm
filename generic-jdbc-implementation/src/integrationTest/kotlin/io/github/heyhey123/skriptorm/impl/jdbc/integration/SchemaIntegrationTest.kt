package io.github.heyhey123.skriptorm.impl.jdbc.integration

import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import kotlinx.coroutines.runBlocking
import java.sql.SQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies that the DDL produced by the dialect is accepted by a real server and creates the
 * declared shape. Mocked tests can only prove which string was sent; only a server proves that the
 * string is valid and means what the implementation intends.
 *
 * This is also where a product the dialect was not written for shows up: registration compares the
 * declaration against the column types the server reports, so a server that names a type differently
 * fails here rather than at the first insert.
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

        // The protocol has no BOOLEAN type: it is an alias for TINYINT(1), and each driver names the
        // column after the alias it was created with — Connector/J says BIT, MariaDB Connector/J says
        // BOOLEAN, and a server that does not carry the alias says TINYINT. All three are the same
        // one-byte column, so the assertion is that it stayed a boolean-shaped integer and not, say, a
        // string. The boolean value itself round trips, which TypeRoundTripIntegrationTest checks.
        assertTrue(
            columns.getValue("active").typeName in setOf("BOOLEAN", "TINYINT", "BIT"),
            "active was reported as ${columns.getValue("active")}"
        )
    }

    /** Fails with the whole column description, so a remote run identifies the column on its own. */
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
     * The reported failure, end to end: a declaration that adds a column to a table that already exists.
     *
     * `CREATE TABLE IF NOT EXISTS` leaves the table alone, so without the comparison the registration
     * succeeds here and the first insert naming `age` is refused by the server with
     * `Unknown column 'age' in 'INSERT INTO'` — a message about the insert, at the insert's line, for a
     * mistake made in the registration.
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
