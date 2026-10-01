package io.github.heyhey123.skriptorm.impl.jdbc.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.impl.jdbc.queries.PinnedConnectionSource
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.sql.Connection
import java.sql.DriverManager
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * What happens to the connection when a transaction ends without committing.
 *
 * This is the mechanism behind the cancellation case in the server suite, which passes on the file
 * backend and failed on MySQL, PostgreSQL and MongoDB: a rollback that does not cover a statement still
 * in flight, followed by a connection handed back to the pool with automatic commits restored — which
 * commits precisely the work the rollback was meant to undo.
 *
 * The mechanism is driven here rather than the symptom, because the symptom needs a network. Two states
 * decide it — a statement still borrowed when the rollback runs, and a statement taken on after the
 * deadline — and both can be produced exactly, in process, on H2. Rolling back is then asked to prove
 * three things: that a connection in either state is thrown away instead of reused, that a clean
 * transaction still recycles its connection, and that a rollback which fails is recorded rather than
 * dropped.
 */
class JdbcTransactionDiscardTest {

    private val database: Database = mockk(relaxed = true)

    private val dialect: JdbcDialect = mockk(relaxed = true)

    private fun h2Connection() = DriverManager.getConnection("jdbc:h2:mem:txdiscard;DB_CLOSE_DELAY=-1")

    private fun pool() = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = "jdbc:h2:mem:txevict;DB_CLOSE_DELAY=-1"
            maximumPoolSize = 1
        }
    )

    /**
     * A connection that agrees with everything except rollback, which either does nothing or fails.
     *
     * Doing nothing is what a driver answering before a statement has landed amounts to: the rollback
     * returns, the transaction believes it is undone, and the statement's work is still there. Failing is
     * what a driver that refuses to roll back while a statement is in flight does instead.
     */
    private fun connectionWhoseRollbackDoesNotWork(delegate: Connection, error: Throwable? = null): Connection =
        Proxy.newProxyInstance(
            Connection::class.java.classLoader,
            arrayOf(Connection::class.java)
        ) { _, method, arguments ->
            if (method.name == "rollback") {
                if (error != null) throw error
                null
            } else {
                try {
                    method.invoke(delegate, *(arguments ?: emptyArray()))
                } catch (invocation: InvocationTargetException) {
                    throw invocation.targetException
                }
            }
        } as Connection

    /**
     * A connection that records what automatic commits were set to and delegates everything else.
     *
     * Read through a recording rather than read back afterwards, because the state that says whether the
     * connection went back reusable — automatic commits on, then closed — cannot be asked of a closed
     * connection: H2 answers with "The object is already closed" instead of the value.
     */
    private fun recordingAutoCommits(delegate: Connection, recorded: MutableList<Boolean>): Connection =
        Proxy.newProxyInstance(
            Connection::class.java.classLoader,
            arrayOf(Connection::class.java)
        ) { _, method, arguments ->
            if (method.name == "setAutoCommit" && arguments?.isNotEmpty() == true) {
                recorded.add(arguments[0] as Boolean)
            }
            try {
                method.invoke(delegate, *(arguments ?: emptyArray()))
            } catch (invocation: InvocationTargetException) {
                throw invocation.targetException
            }
        } as Connection

    /** The lambda is last on purpose: with it earlier, a trailing lambda would bind to [pinned] instead. */
    private fun transaction(
        connection: Connection,
        pinned: PinnedConnectionSource =
            PinnedConnectionSource(connection, deadlineNanos = System.nanoTime() + Duration.ofSeconds(5).toNanos()),
        discard: (Connection) -> Unit
    ) = JdbcTransaction(database, dialect, connection, Duration.ofSeconds(5), discard, pinned)

    @Test
    fun `a clean rollback gives the connection back rather than throwing it away`() {
        val connection = h2Connection()
        connection.autoCommit = false
        val autoCommits = mutableListOf<Boolean>()
        var discarded = false

        runBlocking { transaction(recordingAutoCommits(connection, autoCommits)) { discarded = true }.rollback() }

        assertFalse(discarded, "A transaction with nothing in flight must not cost the pool a connection.")
        assertEquals(
            listOf(true),
            autoCommits,
            "The connection did not go back with automatic commits restored, which is not a reusable state."
        )
        assertTrue(connection.isClosed, "The connection was not returned to the pool.")
    }

    @Test
    fun `a statement still in flight when the rollback runs throws the connection away`() {
        val connection = h2Connection()
        connection.autoCommit = false
        val pinned = PinnedConnectionSource(
            connection,
            deadlineNanos = System.nanoTime() + Duration.ofSeconds(5).toNanos()
        )
        // Exactly the state a network makes likely and a local file does not: the statement has borrowed
        // the connection and has not given it back when the watchdog's rollback arrives.
        pinned.borrow()

        var discarded = false
        runBlocking { transaction(connection, pinned) { discarded = true }.rollback() }

        assertTrue(discarded, "A connection with a statement in flight must not go back to the pool.")
        assertFalse(connection.autoCommit, "Automatic commits were restored, which would commit that statement.")
    }

    @Test
    fun `a statement taken on after the deadline throws the connection away`() {
        val connection = h2Connection()
        connection.autoCommit = false
        // Balanced borrows, so nothing is in flight: what spoils this connection is that a statement was
        // accepted after the deadline, and it may therefore have landed after the rollback.
        val pinned = PinnedConnectionSource(connection, deadlineNanos = System.nanoTime() - 1)
        pinned.borrow()
        pinned.release(connection)

        var discarded = false
        runBlocking { transaction(connection, pinned) { discarded = true }.rollback() }

        assertTrue(discarded, "A statement accepted after the deadline must spoil the connection.")
    }

    @Test
    fun `a rollback that fails is recorded instead of being dropped`() {
        val delegate = h2Connection()
        delegate.autoCommit = false
        val rollbackError = IllegalStateException("the driver refused the rollback")
        val connection = connectionWhoseRollbackDoesNotWork(delegate, rollbackError)
        val timedOut = IllegalStateException("The database transaction was open for longer than 5 seconds.")
        var discarded = false
        val transaction = transaction(connection) { discarded = true }

        // The script's failure is the first one and stays that way: it is what explains the outcome.
        transaction.markFailed(timedOut)
        runBlocking { transaction.rollback() }

        assertSame(timedOut, transaction.failure, "A later failure replaced the one the script is told about.")
        assertTrue(
            transaction.failure?.suppressed?.any { it === rollbackError } == true,
            "The failed rollback was dropped instead of being recorded on the transaction's failure."
        )
        assertTrue(discarded, "A connection whose rollback failed must not go back to the pool.")
    }

    /**
     * Runs the cancellation's shape on a real pool and answers what the pool's next connection can see.
     *
     * The connection the transaction holds does an insert and then has a rollback that does not cover it,
     * because a statement is still borrowed. [release] is then what the release path does with it: the
     * plugin evicts it, and the version this replaced restored automatic commits and closed it — which
     * commits the insert, because changing automatic commits during a transaction commits that
     * transaction. That is the defect in one line, and the second half of the test reproduces it so the
     * first half means something.
     */
    private fun insertSurvivingInto(pool: HikariDataSource, id: Long, release: (Connection) -> Unit): Long {
        val pooled = pool.connection
        val connection = connectionWhoseRollbackDoesNotWork(pooled)
        connection.autoCommit = false
        connection.createStatement().use { it.executeUpdate("insert into evict_probe (id) values ($id)") }
        val pinned = PinnedConnectionSource(
            connection,
            deadlineNanos = System.nanoTime() + Duration.ofSeconds(5).toNanos()
        )
        pinned.borrow()

        // The real pooled connection, not the stand-in, is what a pool can be asked to evict.
        runBlocking { transaction(connection, pinned) { release(pooled) }.rollback() }

        return pool.connection.use { fresh ->
            fresh.createStatement().use { statement ->
                statement.executeQuery("select count(*) from evict_probe where id = $id").use { rows ->
                    rows.next()
                    rows.getLong(1)
                }
            }
        }
    }

    @Test
    fun `work the rollback did not cover never reaches the pool's next connection`() {
        val pool = pool()
        try {
            pool.connection.use { setup ->
                setup.createStatement().use {
                    it.execute("create table if not exists evict_probe (id bigint primary key)")
                }
            }

            assertEquals(
                0L,
                insertSurvivingInto(pool, 1) { pool.evictConnection(it) },
                "The insert the rollback did not cover was committed on the way out and reached the pool."
            )
            assertEquals(
                1L,
                insertSurvivingInto(pool, 2) { suspect ->
                    suspect.autoCommit = true
                    suspect.close()
                },
                "The control did not reproduce the defect, so the assertion above would prove nothing."
            )
        } finally {
            pool.close()
        }
    }
}
