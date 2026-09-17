package io.github.heyhey123.skriptorm.database

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Delete
import io.github.heyhey123.skriptorm.queries.DeleteById
import io.github.heyhey123.skriptorm.queries.InsertIfAbsent
import io.github.heyhey123.skriptorm.queries.InsertMany
import io.github.heyhey123.skriptorm.queries.InsertOne
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.queries.SelectById
import io.github.heyhey123.skriptorm.queries.SelectMany
import io.github.heyhey123.skriptorm.queries.SelectOne
import io.github.heyhey123.skriptorm.queries.SelectPage
import io.github.heyhey123.skriptorm.queries.Update
import io.github.heyhey123.skriptorm.queries.UpdateById
import io.github.heyhey123.skriptorm.queries.UpsertById
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.DataType
import io.github.heyhey123.skriptorm.type.DataTypes
import io.github.heyhey123.skriptorm.type.TypeId
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private val SETTINGS = ConnectionSettings("jdbc:test:transaction", "tester", "secret")

/**
 * Covers the transaction state machine and the lease that makes it safe: which transitions are allowed,
 * what a failure does, what the watchdog does, and what happens to a transaction the connection was
 * closed under.
 *
 * The database here is a fake, so the statements themselves are covered by the JDBC integration suite
 * instead. What is checked here is the part that has to hold whatever the implementation is: a
 * transaction that is open holds the lease, and every way it can end releases it exactly once.
 */
class TransactionTest {

    @BeforeEach
    fun prepareLifecycle() = resetLifecycle()

    @AfterEach
    fun releaseLifecycle() = resetLifecycle()

    @Test
    fun `committing ends the transaction once and gives the connection back`() = runBlocking<Unit> {
        val database = connected()
        val transaction = database.beginTransaction()

        transaction.commit()

        assertEquals(Transaction.State.COMMITTED, transaction.state)
        assertEquals(1, database.commits.get())
        assertEquals(0, database.rollbacks.get())
        assertEquals(1, database.releases.get())
        assertTrue(database.tables.isEmpty())
    }

    @Test
    fun `a committed transaction no longer takes statements`() = runBlocking<Unit> {
        val database = connected()
        val transaction = database.beginTransaction()
        transaction.commit()

        assertFailsWith<TransactionAbortedException> { transaction.withQueries { } }
    }

    @Test
    fun `rolling back twice rolls back once`() = runBlocking<Unit> {
        val database = connected()
        val transaction = database.beginTransaction()

        transaction.rollback()
        transaction.rollback()

        assertEquals(Transaction.State.ROLLED_BACK, transaction.state)
        assertEquals(1, database.rollbacks.get())
        assertEquals(1, database.releases.get())
    }

    @Test
    fun `a failure makes the transaction rollback only and uncommittable`() = runBlocking<Unit> {
        val database = connected()
        val transaction = database.beginTransaction()
        val cause = IllegalStateException("the statement failed")

        transaction.markFailed(cause)

        assertFalse(transaction.isActive)
        assertEquals(Transaction.State.ROLLBACK_ONLY, transaction.state)
        assertSame(cause, transaction.failure)
        assertFailsWith<IllegalStateException> { transaction.commit() }
        assertFailsWith<TransactionAbortedException> { transaction.withQueries { } }
        // Nothing has been rolled back yet: the section is still walking its body, and it rolls the
        // whole thing back once at the end.
        assertEquals(0, database.rollbacks.get())
    }

    @Test
    fun `rollback keeps the failure that caused it`() = runBlocking<Unit> {
        val database = connected()
        val transaction = database.beginTransaction()
        val cause = IllegalStateException("the statement failed")
        transaction.markFailed(cause)

        transaction.rollback()

        assertSame(cause, transaction.failure)
        assertEquals(Transaction.State.ROLLED_BACK, transaction.state)
    }

    @Test
    fun `a failing commit aborts the transaction and reports it`() = runBlocking<Unit> {
        val database = connected()
        database.commitFailure = IllegalStateException("the server went away")
        val transaction = database.beginTransaction()

        val thrown = assertFailsWith<IllegalStateException> { transaction.commit() }

        assertEquals("the server went away", thrown.message)
        assertEquals(Transaction.State.ABORTED, transaction.state)
        assertSame(thrown, transaction.failure)
        assertEquals(1, database.releases.get())
    }

    @Test
    fun `the watchdog rolls a transaction back that stays open too long`() = runBlocking<Unit> {
        val database = connected()
        val transaction = database.beginTransaction(timeout = Duration.ofMillis(50))

        // The watchdog runs on its own scope, so the wait is for it rather than for anything here.
        val deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos()
        while (!transaction.isFinished && System.nanoTime() < deadline) {
            delay(5)
        }

        assertTrue(transaction.isFinished, "the watchdog did not end the transaction")
        assertEquals(Transaction.State.ABORTED, transaction.state)
        assertNotNull(transaction.failure)
        assertEquals(1, database.rollbacks.get())
        assertEquals(1, database.releases.get())
    }

    @Test
    fun `a finished transaction cancels its watchdog`() = runBlocking<Unit> {
        val database = connected()
        val transaction = database.beginTransaction(timeout = Duration.ofMillis(50))

        transaction.commit()
        delay(200)

        assertEquals(Transaction.State.COMMITTED, transaction.state)
        assertEquals(0, database.rollbacks.get())
    }

    @Test
    fun `closing the connection aborts an open transaction instead of waiting for it`() = runBlocking<Unit> {
        val database = connected()
        val transaction = database.beginTransaction()

        // A transaction holds a lease for its whole life, so a disconnect that only waited for leases
        // would wait forever here. This is the case that has to end the transaction by itself.
        withTimeout(Duration.ofSeconds(5).toMillis()) { database.disconnect() }

        assertEquals(Transaction.State.ABORTED, transaction.state)
        assertNotNull(transaction.failure)
        assertEquals(1, database.rollbacks.get())
        assertEquals(1, database.releases.get())
        assertFalse(database.isConnected)
    }

    @Test
    fun `a database that cannot open transactions says so`() = runBlocking<Unit> {
        val database = PlainDatabase()
        Database.connectDefault(database, SETTINGS)

        assertFalse(database.supportsTransactions)
        assertFailsWith<IllegalStateException> { database.beginTransaction() }
    }

    @Test
    fun `a failed begin releases the lease it took`() = runBlocking<Unit> {
        val database = connected()
        database.beginFailure = IllegalStateException("no connection to pin")

        assertFailsWith<IllegalStateException> { database.beginTransaction() }

        // The lease was taken before the pin was attempted, so a disconnect would hang if it leaked.
        withTimeout(Duration.ofSeconds(5).toMillis()) { database.disconnect() }
        assertFalse(database.isConnected)
    }

    // ---------------------------------------------------------------- helpers

    private suspend fun connected(): TransactionalDatabase {
        val database = TransactionalDatabase()
        Database.connectDefault(database, SETTINGS)
        return database
    }

    private fun resetLifecycle() = runBlocking {
        Database.shutdown()
        Database.beginLifecycle()
    }
}

/** A database whose transactions record what was done to them instead of touching a server. */
private class TransactionalDatabase : Database() {

    override val dataTypes: DataTypes = TransactionNoDataTypes

    override val supportsTransactions: Boolean = true

    val commits = AtomicInteger()
    val rollbacks = AtomicInteger()
    val releases = AtomicInteger()

    @Volatile
    var beginFailure: Throwable? = null

    @Volatile
    var commitFailure: Throwable? = null

    override fun doConnect(settings: ConnectionSettings) {
        queries = TransactionQueries
    }

    override fun doDisconnect() = Unit

    override suspend fun doRegisterTable(table: Table) = Unit

    override fun doBeginTransaction(timeout: Duration): Transaction {
        beginFailure?.let { throw it }
        return RecordingTransaction(this, timeout)
    }
}

/** A database that never claims to support transactions, which is the default. */
private class PlainDatabase : Database() {

    override val dataTypes: DataTypes = TransactionNoDataTypes

    override fun doConnect(settings: ConnectionSettings) {
        queries = TransactionQueries
    }

    override fun doDisconnect() = Unit

    override suspend fun doRegisterTable(table: Table) = Unit
}

private class RecordingTransaction(
    database: Database,
    timeout: Duration
) : Transaction(database, timeout) {

    private val owner: TransactionalDatabase
        get() = database as TransactionalDatabase

    override val queries: Queries = TransactionQueries

    override suspend fun doCommit() {
        owner.commitFailure?.let { throw it }
        owner.commits.incrementAndGet()
    }

    override suspend fun doRollback() {
        owner.rollbacks.incrementAndGet()
    }

    override suspend fun doRelease() {
        owner.releases.incrementAndGet()
    }
}

private object TransactionNoDataTypes : DataTypes() {
    override val typesRegistry = mutableMapOf<TypeId, DataType<*>>()
}

/** Satisfies the lifecycle's "queries must be initialized" check; no test here executes a query. */
private object TransactionQueries : Queries {

    override fun selectById(id: Any): SelectById = unusedTransactionQuery()
    override fun selectOne(where: WhereClause?): SelectOne = unusedTransactionQuery()
    override fun selectMany(where: WhereClause?): SelectMany = unusedTransactionQuery()
    override fun selectPage(pageSize: Int, pageIndex: Int, where: WhereClause?): SelectPage = unusedTransactionQuery()
    override fun insertOne(values: Map<String, Any?>): InsertOne = unusedTransactionQuery()
    override fun insertMany(valuesList: List<Map<String, Any?>>): InsertMany = unusedTransactionQuery()
    override fun insertIfAbsent(values: Map<String, Any?>): InsertIfAbsent = unusedTransactionQuery()
    override fun update(values: Map<String, Any?>, limit: Int?, where: WhereClause?): Update = unusedTransactionQuery()
    override fun updateById(id: Any, values: Map<String, Any?>): UpdateById = unusedTransactionQuery()
    override fun upsertById(id: Any, values: Map<String, Any?>): UpsertById = unusedTransactionQuery()
    override fun delete(limit: Int?, where: WhereClause?): Delete = unusedTransactionQuery()
    override fun deleteById(id: Any): DeleteById = unusedTransactionQuery()
}

private fun unusedTransactionQuery(): Nothing =
    throw UnsupportedOperationException("The transaction tests never execute a query.")
