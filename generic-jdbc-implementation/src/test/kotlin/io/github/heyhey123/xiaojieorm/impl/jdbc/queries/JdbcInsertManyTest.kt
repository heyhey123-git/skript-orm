package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.GenericJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.Statement
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
            statement.setObject(1, 10, JDBCType.INTEGER)
            statement.setObject(2, 1, JDBCType.INTEGER)
            statement.addBatch()
            statement.setObject(1, 20, JDBCType.INTEGER)
            statement.setObject(2, 2, JDBCType.INTEGER)
            statement.addBatch()
            statement.executeLargeBatch()
        }
        verify { statement.close() }
        verify { source.release(connection) }
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
}
