package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.impl.jdbc.database.GenericJdbcDialect
import io.github.heyhey123.skriptorm.impl.jdbc.database.MysqlJdbcDialect
import io.github.heyhey123.skriptorm.impl.jdbc.database.MysqlServerJdbcDialect
import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.UuidJdbcDataType
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class JdbcInsertManyTest {

    private val table = Table(
        "items",
        listOf(
            Column("id", IntJdbcDataType(), isPrimaryKey = true),
            Column("value", IntJdbcDataType())
        )
    )

    @Test
    fun `empty batch succeeds without borrowing connection`() = runBlocking {
        val source = mockk<JdbcConnectionSource>(relaxed = true)

        val result = JdbcInsertMany(emptyList(), source, GenericJdbcDialect).execute(table)

        assertEquals(WriteResult(0), result)
        verify(exactly = 0) { source.borrow() }
    }

    @Test
    fun `rows bind in first row column order and successful counts are summed`() = runBlocking {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val source = mockk<JdbcConnectionSource>(relaxed = true)
        every { source.borrow() } returns connection
        every { connection.prepareStatement("INSERT INTO \"items\" (\"value\", \"id\") VALUES (?, ?)") } returns statement
        every { statement.executeLargeBatch() } returns longArrayOf(1, 2)

        val result = JdbcInsertMany(
            listOf(
                linkedMapOf("value" to 10, "id" to 1),
                linkedMapOf("id" to 2, "value" to 20)
            ),
            source,
            GenericJdbcDialect
        ).execute(table)

        assertEquals(WriteResult(3), result)
        verifyOrder {
            statement.setObject(1, 10, JDBCType.INTEGER.vendorTypeNumber)
            statement.setObject(2, 1, JDBCType.INTEGER.vendorTypeNumber)
            statement.addBatch()
            statement.setObject(1, 20, JDBCType.INTEGER.vendorTypeNumber)
            statement.setObject(2, 2, JDBCType.INTEGER.vendorTypeNumber)
            statement.addBatch()
            statement.executeLargeBatch()
        }
        verify { statement.close() }
        verify { source.release(connection) }
    }

    @Test
    fun `MySQL binds mixed and nullable values in one multi-row statement`() = runBlocking {
        val typedTable = Table(
            "items",
            listOf(Column("id", IntJdbcDataType()), Column("name", StringJdbcDataType()), Column("age", IntJdbcDataType()))
        )
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val source = mockk<JdbcConnectionSource>(relaxed = true)
        every { source.borrow() } returns connection
        every { source.statementTimeoutSeconds() } returns 3
        val (packetStatement, packetResult) = packetProbe(connection)
        every {
            connection.prepareStatement("INSERT INTO `items` (`name`, `id`, `age`) VALUES (?, ?, ?), (?, ?, ?)")
        } returns statement
        every { statement.executeLargeUpdate() } returns 2L

        val result = JdbcInsertMany(
            listOf(
                linkedMapOf("name" to "first", "id" to 1, "age" to null),
                linkedMapOf("age" to 22, "id" to 2, "name" to "second")
            ),
            source,
            MysqlServerJdbcDialect
        ).execute(typedTable)

        assertEquals(WriteResult(2), result)
        verifyOrder {
            statement.setObject(1, "first", JDBCType.VARCHAR.vendorTypeNumber)
            statement.setObject(2, 1, JDBCType.INTEGER.vendorTypeNumber)
            statement.setNull(3, JDBCType.INTEGER.vendorTypeNumber)
            statement.setObject(4, "second", JDBCType.VARCHAR.vendorTypeNumber)
            statement.setObject(5, 2, JDBCType.INTEGER.vendorTypeNumber)
            statement.setObject(6, 22, JDBCType.INTEGER.vendorTypeNumber)
            statement.executeLargeUpdate()
        }
        verify { statement.queryTimeout = 3 }
        verify { packetStatement.queryTimeout = 3 }
        verify { packetResult.close() }
        verify { packetStatement.close() }
        verify(exactly = 0) { statement.addBatch() }
        verify(exactly = 0) { statement.executeLargeBatch() }
        verify { statement.close() }
        verify { source.release(connection) }
    }

    @Test
    fun `MySQL direct query splits more than 30000 bound values`() = runBlocking {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val source = mockk<JdbcConnectionSource>(relaxed = true)
        val statements = mutableListOf<String>()
        every { source.borrow() } returns connection
        packetProbe(connection)
        every { connection.prepareStatement(match { it.startsWith("INSERT") }) } answers {
            statements += firstArg<String>()
            statement
        }
        every { statement.executeLargeUpdate() } returnsMany listOf(30_000L, 1L)

        val result = JdbcInsertMany(
            List(30_001) { mapOf("id" to it) },
            source,
            MysqlServerJdbcDialect
        ).execute(table)

        assertEquals(WriteResult(30_001), result)
        assertEquals(2, statements.size)
        assertEquals(30_000, statements[0].count { it == '?' })
        assertEquals("INSERT INTO `items` (`id`) VALUES (?)", statements[1])
        verify(exactly = 2) { statement.executeLargeUpdate() }
        verify(exactly = 1) { source.borrow() }
        verify(exactly = 1) { source.release(connection) }
    }

    @Test
    fun `MySQL splits strings to stay within the packet budget`() = runBlocking {
        val textTable = Table("items", listOf(Column("name", StringJdbcDataType())))
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val source = mockk<JdbcConnectionSource>(relaxed = true)
        val insertSql = mutableListOf<String>()
        val baseSql = MysqlServerJdbcDialect.insertMany("items", listOf("name"), 1)
        val twoRowBudget = baseSql.length.toLong() * 4L + 1024L + 2L * (16L + 16L + 88L)
        every { source.borrow() } returns connection
        packetProbe(connection, twoRowBudget)
        every { connection.prepareStatement(match { it.startsWith("INSERT") }) } answers {
            insertSql += firstArg<String>()
            statement
        }
        every { statement.executeLargeUpdate() } returnsMany listOf(2L, 1L)

        val result = JdbcInsertMany(
            List(3) { mapOf("name" to "abcdefghij") },
            source,
            MysqlServerJdbcDialect
        ).execute(textTable)

        assertEquals(WriteResult(3), result)
        assertEquals(MysqlServerJdbcDialect.insertMany("items", listOf("name"), 2), insertSql[0])
        assertEquals(baseSql, insertSql[1])
        verify(exactly = 1) { source.borrow() }
        verify(exactly = 1) { source.release(connection) }
    }

    @Test
    fun `MySQL keeps rows with null custom-converter values in one statement`() = runBlocking {
        val uuidTable = Table(
            "items",
            listOf(Column("id", IntJdbcDataType()), Column("uid", UuidJdbcDataType()))
        )
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val source = mockk<JdbcConnectionSource>(relaxed = true)
        val insertSql = mutableListOf<String>()
        every { source.borrow() } returns connection
        packetProbe(connection)
        every { connection.prepareStatement(match { it.startsWith("INSERT") }) } answers {
            insertSql += firstArg<String>()
            statement
        }
        every { statement.executeLargeUpdate() } returns 2L

        val result = JdbcInsertMany(
            listOf(mapOf("id" to 1, "uid" to null), mapOf("id" to 2, "uid" to null)),
            source,
            MysqlServerJdbcDialect
        ).execute(uuidTable)

        assertEquals(WriteResult(2), result)
        assertEquals(listOf(MysqlServerJdbcDialect.insertMany("items", listOf("id", "uid"), 2)), insertSql)
        verifyOrder {
            statement.setObject(1, 1, JDBCType.INTEGER.vendorTypeNumber)
            statement.setNull(2, JDBCType.BINARY.vendorTypeNumber)
            statement.setObject(3, 2, JDBCType.INTEGER.vendorTypeNumber)
            statement.setNull(4, JDBCType.BINARY.vendorTypeNumber)
            statement.executeLargeUpdate()
        }
        verify(exactly = 1) { statement.executeLargeUpdate() }
        verify(exactly = 1) { source.release(connection) }
    }

    @Test
    fun `MySQL sends custom-converter rows separately`() = runBlocking {
        val uuidTable = Table("items", listOf(Column("uid", UuidJdbcDataType())))
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val source = mockk<JdbcConnectionSource>(relaxed = true)
        val insertSql = mutableListOf<String>()
        every { source.borrow() } returns connection
        packetProbe(connection)
        every { connection.prepareStatement(match { it.startsWith("INSERT") }) } answers {
            insertSql += firstArg<String>()
            statement
        }
        every { statement.executeLargeUpdate() } returns 1L

        val result = JdbcInsertMany(
            List(2) { mapOf("uid" to UUID.randomUUID()) },
            source,
            MysqlServerJdbcDialect
        ).execute(uuidTable)

        assertEquals(WriteResult(2), result)
        assertEquals(List(2) { MysqlServerJdbcDialect.insertMany("items", listOf("uid"), 1) }, insertSql)
        verify(exactly = 2) { statement.executeLargeUpdate() }
    }

    @Test
    fun `invalid MySQL packet limit closes the probe and releases the connection`() {
        val connection = mockk<Connection>(relaxed = true)
        val source = mockk<JdbcConnectionSource>(relaxed = true)
        every { source.borrow() } returns connection
        val (packetStatement, packetResult) = packetProbe(connection, 0L)

        val failure = assertFailsWith<IllegalStateException> {
            runBlocking { JdbcInsertMany(listOf(mapOf("id" to 1)), source, MysqlServerJdbcDialect).execute(table) }
        }

        assertEquals("MySQL returned an invalid max_allowed_packet: 0.", failure.message)
        verify { packetResult.close() }
        verify { packetStatement.close() }
        verify { source.release(connection) }
        verify(exactly = 1) { connection.prepareStatement(any()) }
    }

    @Test
    fun `MariaDB dialect keeps the JDBC batch path`() = runBlocking {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val source = mockk<JdbcConnectionSource>(relaxed = true)
        every { source.borrow() } returns connection
        every { connection.prepareStatement("INSERT INTO `items` (`id`) VALUES (?)") } returns statement
        every { statement.executeLargeBatch() } returns longArrayOf(1, 1)

        val result = JdbcInsertMany(listOf(mapOf("id" to 1), mapOf("id" to 2)), source, MysqlJdbcDialect)
            .execute(table)

        assertEquals(WriteResult(2), result)
        verify(exactly = 2) { statement.addBatch() }
        verify(exactly = 1) { statement.executeLargeBatch() }
        verify(exactly = 0) { statement.executeLargeUpdate() }
    }

    @Test
    fun `success no info marks count inexact without inventing count`() = runBlocking {
        val (query, statement) = queryWithCounts(longArrayOf(2, Statement.SUCCESS_NO_INFO.toLong()))

        val result = query.execute(table)

        assertEquals(2, result.affectedCount)
        assertFalse(result.countExact)
        verify { statement.executeLargeBatch() }
    }

    @Test
    fun `execute failed and unknown negative counts fail`() {
        listOf(Statement.EXECUTE_FAILED.toLong(), -99L).forEach { count ->
            val (query, _) = queryWithCounts(longArrayOf(count))
            assertFailsWith<IllegalStateException> { runBlocking { query.execute(table) } }
        }
    }

    @Test
    fun `batch rejects empty rows and inconsistent column sets before prepare`() {
        val source = mockk<JdbcConnectionSource>(relaxed = true)
        assertFailsWith<IllegalArgumentException> {
            runBlocking { JdbcInsertMany(listOf(emptyMap()), source, GenericJdbcDialect).execute(table) }
        }
        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                JdbcInsertMany(
                    listOf(mapOf("id" to 1), mapOf("value" to 2)),
                    source,
                    GenericJdbcDialect
                ).execute(table)
            }
        }
        verify(exactly = 0) { source.borrow() }
    }

    private fun queryWithCounts(counts: LongArray): Pair<JdbcInsertMany, PreparedStatement> {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val source = mockk<JdbcConnectionSource>(relaxed = true)
        every { source.borrow() } returns connection
        every { connection.prepareStatement(any()) } returns statement
        every { statement.executeLargeBatch() } returns counts
        return JdbcInsertMany(listOf(mapOf("id" to 1)), source, GenericJdbcDialect) to statement
    }

    private fun packetProbe(
        connection: Connection,
        limit: Long = 64L * 1024L * 1024L
    ): Pair<PreparedStatement, ResultSet> {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val result = mockk<ResultSet>(relaxed = true)
        every { connection.prepareStatement("SELECT @@max_allowed_packet") } returns statement
        every { statement.executeQuery() } returns result
        every { result.next() } returns true
        every { result.getLong(1) } returns limit
        return statement to result
    }
}
