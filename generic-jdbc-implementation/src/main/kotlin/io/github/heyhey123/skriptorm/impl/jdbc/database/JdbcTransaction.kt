package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.database.Transaction
import io.github.heyhey123.skriptorm.impl.jdbc.queries.JdbcQueries
import io.github.heyhey123.skriptorm.impl.jdbc.queries.PinnedConnectionSource
import io.github.heyhey123.skriptorm.queries.Queries
import java.sql.Connection
import java.time.Duration

/**
 * A transaction on one JDBC connection, with automatic commits turned off for as long as it lives.
 *
 * The connection is borrowed from the pool for the whole transaction and returned once, in
 * [doRelease], which is what makes the statements inside it see each other's uncommitted work.
 */
class JdbcTransaction(
    database: Database,
    dialect: JdbcDialect,
    private val connection: Connection,
    timeout: Duration,
    /**
     * Removes a connection from the pool for good, closing it rather than handing it to the next caller.
     *
     * The transaction does not know what the pool is, so the database that made it says how to throw one
     * away. Required rather than defaulted: a default that closed the connection would look like a
     * discard on a raw connection and would quietly recycle a pooled one, which is the case this exists
     * to prevent.
     */
    private val discardConnection: (Connection) -> Unit,
    /**
     * The source the statements inside this transaction run on.
     *
     * A parameter rather than something built in place so that the state deciding whether the connection
     * may be reused — a statement still in flight, or one taken on after the deadline — can be driven in
     * a test without a network and without a pool.
     */
    private val pinned: PinnedConnectionSource =
        PinnedConnectionSource(connection, deadlineNanos = System.nanoTime() + timeout.toNanos())
) : Transaction(database, timeout) {

    override val queries: Queries = JdbcQueries(pinned, dialect)

    /**
     * Whether the pinned connection may still carry a statement whose work the rollback did not cover.
     *
     * A statement still running when the watchdog fires is the case this is for: the rollback is issued
     * while it is in flight, and whatever it wrote afterwards must not be committed by the release path.
     */
    override fun hasStatementInFlight(): Boolean = pinned.mustNotBeReused

    override suspend fun doCommit() {
        connection.commit()
    }

    override suspend fun doRollback() {
        connection.rollback()
    }

    /**
     * Hands the connection back, or throws it away when its state cannot be trusted.
     *
     * The normal path returns the connection to the pool in a state the next caller can use. Every step
     * is attempted even when an earlier one fails, and the first failure is the one thrown, the same way
     * a cursor releases its resources. Leaving automatic commits off would hand a connection to the next
     * script with an open transaction on it, which is worse than the failure being reported.
     *
     * The discard path is for a transaction whose undo cannot be trusted — the rollback failed, or a
     * statement was still running while it was issued. Restoring automatic commits on that connection is
     * what would commit the work the transaction meant to undo, so it is closed instead and the pool
     * gives the next statement a fresh one.
     */
    override suspend fun doRelease() {
        if (mustDiscardConnection) {
            discardConnection(connection)
            return
        }
        var failure: Throwable? = null
        try {
            if (!connection.autoCommit) connection.rollback()
        } catch (error: Throwable) {
            failure = error
        }
        try {
            connection.autoCommit = true
        } catch (error: Throwable) {
            if (failure == null) failure = error else failure.addSuppressed(error)
        }
        try {
            connection.close()
        } catch (error: Throwable) {
            if (failure == null) failure = error else failure.addSuppressed(error)
        }
        failure?.let { throw it }
    }
}
