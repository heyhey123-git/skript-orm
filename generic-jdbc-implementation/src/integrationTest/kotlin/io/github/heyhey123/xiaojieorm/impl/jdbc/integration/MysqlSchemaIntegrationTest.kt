package io.github.heyhey123.xiaojieorm.impl.jdbc.integration

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table
import kotlinx.coroutines.runBlocking
import java.sql.SQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies that the DDL produced by `MysqlJdbcDialect` is accepted by a real MySQL server and
 * creates the declared shape. Mocked tests can only prove which string was sent; only a server
 * proves that the string is valid and means what the implementation intends.
 */
class MysqlSchemaIntegrationTest : MysqlIntegrationTestBase() {

    @Test
    fun `registerTable creates every declared column`() = runBlocking<Unit> {
        recreateTable()

        val columns = columnMetadata("users")
        assertEquals(usersTable.columns.keys, columns.map { it.name }.toSet())
        assertEquals(usersTable.columns.size, columns.size)
    }

    @Test
    fun `declared column types and sizes survive the round trip to mysql`() = runBlocking<Unit> {
        recreateTable()
        val columns = columnMetadata("users").associateBy { it.name }

        assertEquals("VARCHAR", columns.getValue("name").typeName)
        assertEquals(100, columns.getValue("name").size)
        assertEquals("INT", columns.getValue("id").typeName)
        assertEquals("INT", columns.getValue("age").typeName)
        assertEquals("BIGINT", columns.getValue("big").typeName)
        assertEquals("TINYINT", columns.getValue("tiny").typeName)
        assertEquals("DOUBLE", columns.getValue("score").typeName)
        assertEquals("FLOAT", columns.getValue("ratio").typeName)
        // MySQL treats BOOLEAN as an alias for TINYINT(1).
        assertEquals("TINYINT", columns.getValue("active").typeName)
        assertEquals("BINARY", columns.getValue("uid").typeName)
        assertEquals(16, columns.getValue("uid").size)
    }

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
    fun `the declared primary key is the one mysql enforces`() = runBlocking<Unit> {
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
