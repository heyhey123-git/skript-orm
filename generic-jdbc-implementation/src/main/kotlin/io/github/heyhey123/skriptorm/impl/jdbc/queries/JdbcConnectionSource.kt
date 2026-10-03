package io.github.heyhey123.skriptorm.impl.jdbc.queries

import java.sql.Connection
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource
import kotlin.math.ceil

/**
 * Supplies JDBC statements with either pooled connections or a connection pinned to a transaction.
 * The caller releases each borrowed connection through the source that supplied it.
 */
interface JdbcConnectionSource {

    /** Borrows a connection. The caller owes it to [release] when it is done with it. */
    fun borrow(): Connection

    /** Gives a borrowed connection back. */
    fun release(connection: Connection)

    /**
     * Timeout in seconds for the next statement. Zero keeps the driver's default. A transaction
     * source calculates this from its remaining time each time a statement starts.
     */
    fun statementTimeoutSeconds(): Int = 0
}

/** One connection per statement, borrowed from a pool and returned to it. */
class PooledConnectionSource(
    private val dataSource: DataSource,
    private val timeoutSeconds: Int = 0
) : JdbcConnectionSource {

    override fun borrow(): Connection = dataSource.connection

    override fun release(connection: Connection) {
        connection.close()
    }

    override fun statementTimeoutSeconds(): Int = timeoutSeconds
}

/**
 * Supplies the connection held by a transaction. [release] leaves it open for the transaction to
 * close. Statement timeouts use the remaining time until [deadlineNanos], rounded up to at least
 * one second.
 */
class PinnedConnectionSource(
    private val connection: Connection,
    private val deadlineNanos: Long
) : JdbcConnectionSource {

    private val inFlight = AtomicInteger()

    @Volatile
    private var borrowedAtOrAfterDeadline = false

    override fun borrow(): Connection {
        // Keep track of statements started after the deadline so the transaction discards the
        // connection even if that statement finishes before rollback.
        if (System.nanoTime() >= deadlineNanos) borrowedAtOrAfterDeadline = true
        inFlight.incrementAndGet()
        return connection
    }

    override fun release(connection: Connection) {
        inFlight.decrementAndGet()
    }

    /**
     * True if a statement may still be running or started after the deadline. In either case,
     * rollback may not cover all its work, so the transaction must discard the connection.
     */
    val mustNotBeReused: Boolean
        get() = inFlight.get() > 0 || borrowedAtOrAfterDeadline

    override fun statementTimeoutSeconds(): Int {
        val remainingSeconds = (deadlineNanos - System.nanoTime()) / NANOS_PER_SECOND
        return ceil(remainingSeconds).coerceAtLeast(1.0).toInt()
    }

    private companion object {
        const val NANOS_PER_SECOND = 1_000_000_000.0
    }
}
