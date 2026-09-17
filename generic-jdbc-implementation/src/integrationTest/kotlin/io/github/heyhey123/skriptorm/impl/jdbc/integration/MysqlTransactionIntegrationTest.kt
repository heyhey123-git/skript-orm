package io.github.heyhey123.skriptorm.impl.jdbc.integration

import io.github.heyhey123.skriptorm.database.Transaction
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Covers transactions against a real server: that a pinned connection carries every statement of the
 * transaction, that nothing is visible outside it until it commits, and that the connection goes back
 * to the pool in a usable state whichever way it ended.
 *
 * The base class opens one pooled connection for the test; `rawRowCount` borrows another one from the
 * same pool, so its reads happen outside the transaction without a second server.
 */
class MysqlTransactionIntegrationTest : MysqlIntegrationTestBase() {

    @Test
    fun `a committed transaction keeps what it wrote`() = runBlocking<Unit> {
        recreateTable()
        val transaction = database.beginTransaction()

        transaction.withQueries { it.insertOne(userValues("committed")).execute(usersTable) }

        transaction.commit()

        assertEquals(1, rawRowCount())
        awaitIdleConnections()
    }

    @Test
    fun `a rolled back transaction leaves nothing behind`() = runBlocking<Unit> {
        recreateTable()
        val transaction = database.beginTransaction()
        transaction.withQueries { it.insertOne(userValues("rolled-back")).execute(usersTable) }

        transaction.rollback()

        assertEquals(0, rawRowCount())
        awaitIdleConnections()
    }

    @Test
    fun `a transaction sees its own writes before anybody else does`() = runBlocking<Unit> {
        recreateTable()
        val transaction = database.beginTransaction()
        transaction.withQueries { it.insertOne(userValues("uncommitted")).execute(usersTable) }

        val inside = transaction.withQueries {
            it.selectMany(null).execute(usersTable).readColumn("name", StringJdbcDataType())
        }
        assertEquals(listOf("uncommitted"), inside)
        assertEquals(0, rawRowCount(), "an uncommitted row must not be visible on another connection")

        transaction.commit()
        assertEquals(1, rawRowCount())
    }

    /**
     * A read inside a transaction closes its cursor, and that cursor must not close the connection it
     * ran on: the writes after it are still part of the same transaction, and the rollback has to undo
     * them all.
     */
    @Test
    fun `a cursor inside a transaction leaves the connection pinned`() = runBlocking<Unit> {
        recreateTable()
        val transaction = database.beginTransaction()
        transaction.withQueries { it.insertOne(userValues("first")).execute(usersTable) }

        val names = transaction.withQueries {
            it.selectMany(null).execute(usersTable).readColumn("name", StringJdbcDataType())
        }
        assertEquals(listOf("first"), names)
        transaction.withQueries { it.insertOne(userValues("second")).execute(usersTable) }

        transaction.rollback()

        assertEquals(0, rawRowCount(), "everything the transaction wrote has to be gone")
        awaitIdleConnections()
    }

    @Test
    fun `statements after a transaction go back to the pool`() = runBlocking<Unit> {
        recreateTable()
        val transaction = database.beginTransaction()
        transaction.commit()

        val written = queries.insertOne(userValues("after")).execute(usersTable)

        assertEquals(1L, written.affectedCount)
        assertEquals(1, rawRowCount())
        awaitIdleConnections()
    }

    @Test
    fun `a transaction that is still open is rolled back when the connection closes`() = runBlocking<Unit> {
        recreateTable()
        val transaction = database.beginTransaction()
        transaction.withQueries { it.insertOne(userValues("abandoned")).execute(usersTable) }

        database.disconnect()

        assertEquals(Transaction.State.ABORTED, transaction.state)
        assertNotNull(transaction.failure)
    }
}
