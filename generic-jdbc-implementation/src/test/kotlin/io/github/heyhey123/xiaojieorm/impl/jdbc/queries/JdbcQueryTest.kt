package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.Condition
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.GenericJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLTimeoutException
import javax.sql.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class JdbcQueryTest {

    private val table = Table("items", listOf(Column("id", IntJdbcDataType(), isPrimaryKey = true)))

    @Test
    fun `bind where starts at requested offset skips null and returns next index`() {
        val query = TestJdbcQuery(mockk<JdbcConnectionSource>(relaxed = true))
        val statement = mockk<PreparedStatement>(relaxed = true)
        val where = WhereClause.All(
            false,
            listOf(Condition.Equals("id", null), Condition.Between("id", 2, 3))
        )

        val next = query.bindWhere(table, where, statement, startIndex = 4)

        assertEquals(6, next)
        verify { statement.setObject(4, 2, JDBCType.INTEGER) }
        verify { statement.setObject(5, 3, JDBCType.INTEGER) }
    }

    @Test
    fun `bind where rejects an unknown column before binding`() {
        val query = TestJdbcQuery(mockk<JdbcConnectionSource>(relaxed = true))
        val statement = mockk<PreparedStatement>(relaxed = true)

        assertFailsWith<IllegalArgumentException> {
            query.bindWhere(table, WhereClause.All(false, listOf(Condition.Equals("missing", 1))), statement)
        }
        verify(exactly = 0) { statement.setObject(any(), any(), any<JDBCType>()) }
    }

    @Test
    fun `the timeout the connection source asks for is applied to the statement`() {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val source = PooledConnectionSource(mockk(relaxed = true), timeoutSeconds = 4)

        val applied = TestJdbcQuery(source).configureStatement(statement)

        assertEquals(4, applied)
        verify { statement.queryTimeout = 4 }
    }

    @Test
    fun `a source that asks for no timeout leaves the driver's default alone`() {
        val statement = mockk<PreparedStatement>(relaxed = true)

        val applied = TestJdbcQuery(PooledConnectionSource(mockk(relaxed = true))).configureStatement(statement)

        assertEquals(0, applied)
        verify(exactly = 0) { statement.queryTimeout = any() }
    }

    @Test
    fun `a negative timeout from a source is rejected`() {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val source = PooledConnectionSource(mockk(relaxed = true), timeoutSeconds = -1)

        assertFailsWith<IllegalArgumentException> {
            TestJdbcQuery(source).configureStatement(statement)
        }
    }

    /**
     * What the driver throws says only that the statement was cancelled. The script author needs the two
     * things it does not say: how long the statement was given, and that the connection is still usable.
     */
    @Test
    fun `a cancelled statement says how long it was given`() {
        val connection = mockk<Connection>(relaxed = true)
        val statement = mockk<PreparedStatement>(relaxed = true)
        val dataSource = mockk<DataSource>()
        every { dataSource.connection } returns connection
        val source = PooledConnectionSource(dataSource, timeoutSeconds = 30)
        every { connection.prepareStatement(any()) } returns statement
        every { statement.executeQuery() } throws SQLTimeoutException("Statement cancelled", "HYT00", 0)

        val thrown = assertFailsWith<SQLTimeoutException> {
            runBlocking { TestJdbcQuery(source).executeCursor("SELECT") {} }
        }

        assertTrue("did not finish within 30 second(s)" in (thrown.message ?: ""), thrown.message ?: "")
        assertTrue("still usable" in (thrown.message ?: ""), thrown.message ?: "")
    }

    @Test
    fun `execute update closes statement and connection and returns large count`() = runBlocking {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val dataSource = mockk<DataSource>()
        every { dataSource.connection } returns connection
        every { connection.prepareStatement("UPDATE") } returns statement
        every { statement.executeLargeUpdate() } returns 7
        val query = TestJdbcQuery(PooledConnectionSource(dataSource))

        val result = query.executeUpdate("UPDATE") { it.setObject(1, 1, JDBCType.INTEGER) }

        assertEquals(7, result.affectedCount)
        verify { statement.close() }
        verify { connection.close() }
    }

    @Test
    fun `update bind failure remains primary while statement and connection still close`() {
        val bindFailure = IllegalStateException("bind")
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val dataSource = mockk<DataSource>()
        every { dataSource.connection } returns connection
        every { connection.prepareStatement(any()) } returns statement

        val thrown = assertFailsWith<IllegalStateException> {
            runBlocking {
                TestJdbcQuery(PooledConnectionSource(dataSource)).executeUpdate("UPDATE") { throw bindFailure }
            }
        }

        assertSame(bindFailure, thrown)
        verify { statement.close() }
        verify { connection.close() }
    }

    @Test
    fun `prepare statement failure closes borrowed connection`() {
        val prepareFailure = IllegalStateException("prepare")
        val connection = mockk<Connection>(relaxed = true)
        val dataSource = mockk<DataSource>()
        every { dataSource.connection } returns connection
        every { connection.prepareStatement(any()) } throws prepareFailure

        val thrown = assertFailsWith<IllegalStateException> {
            runBlocking { TestJdbcQuery(PooledConnectionSource(dataSource)).executeCursor("SELECT") {} }
        }

        assertSame(prepareFailure, thrown)
        verify { connection.close() }
    }

    @Test
    fun `execute cursor transfers resource ownership until cursor close`() = runBlocking {
        val resultSet = mockk<ResultSet>(relaxed = true)
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val dataSource = mockk<DataSource>()
        every { dataSource.connection } returns connection
        every { connection.prepareStatement("SELECT") } returns statement
        every { statement.executeQuery() } returns resultSet
        val result = TestJdbcQuery(PooledConnectionSource(dataSource)).executeCursor("SELECT") {}

        verify(exactly = 0) { resultSet.close() }
        verify(exactly = 0) { statement.close() }
        verify(exactly = 0) { connection.close() }

        result.cursor.close()

        verify { resultSet.close() }
        verify { statement.close() }
        verify { connection.close() }
    }

    @Test
    fun `cursor execution failure closes resources and preserves primary failure`() {
        val resultSetFailure = IllegalStateException("query")
        val closeFailure = IllegalStateException("statement close")
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        val dataSource = mockk<DataSource>()
        every { dataSource.connection } returns connection
        every { connection.prepareStatement(any()) } returns statement
        every { statement.executeQuery() } throws resultSetFailure
        every { statement.close() } throws closeFailure

        val thrown = assertFailsWith<IllegalStateException> {
            runBlocking { TestJdbcQuery(PooledConnectionSource(dataSource)).executeCursor("SELECT") {} }
        }

        assertSame(resultSetFailure, thrown)
        assertEquals(listOf(closeFailure), thrown.suppressed.toList())
        verify { connection.close() }
    }

    /**
     * The other half of the same contract: a cursor inside a transaction closes its own resources and
     * leaves the connection, because that connection outlives it.
     */
    @Test
    fun `a pinned source hands back the same connection and a cursor close leaves it open`() = runBlocking {
        val resultSet = mockk<ResultSet>(relaxed = true)
        val statement = mockk<PreparedStatement>(relaxed = true)
        val connection = mockk<Connection>(relaxed = true)
        every { connection.prepareStatement("SELECT") } returns statement
        every { statement.executeQuery() } returns resultSet
        val source = PinnedConnectionSource(connection, deadlineNanos = deadlineIn(30))
        val query = TestJdbcQuery(source)

        assertSame(connection, source.borrow())

        val result = query.executeCursor("SELECT") {}
        result.cursor.close()

        verify { resultSet.close() }
        verify { statement.close() }
        verify(exactly = 0) { connection.close() }
    }

    /**
     * A transaction's timeout is a deadline, not a duration: the statement is given what is left of it,
     * so the last statement of a transaction cannot outlive the transaction by a whole timeout.
     */
    @Test
    fun `a pinned source answers with what is left of its deadline`() {
        val connection = mockk<Connection>(relaxed = true)
        val withinThirtySeconds = PinnedConnectionSource(connection, deadlineNanos = deadlineIn(30))
        val alreadyPast = PinnedConnectionSource(connection, deadlineNanos = System.nanoTime() - 1)

        assertTrue(withinThirtySeconds.statementTimeoutSeconds() in 29..30)
        // Nothing left is still one second: a statement that started too late has to give up rather than
        // run without a bound, which is what lets the transaction's rollback proceed.
        assertEquals(1, alreadyPast.statementTimeoutSeconds())
    }

    @Test
    fun `a pinned source releases a borrowed connection without closing it`() {
        val connection = mockk<Connection>(relaxed = true)
        val source = PinnedConnectionSource(connection, deadlineNanos = deadlineIn(30))

        source.release(connection)

        verify(exactly = 0) { connection.close() }
    }

    private fun deadlineIn(seconds: Long): Long = System.nanoTime() + seconds * 1_000_000_000L

    private class TestJdbcQuery(
        override val connectionSource: JdbcConnectionSource,
        override val dialect: JdbcDialect = GenericJdbcDialect
    ) : JdbcQuery
}
