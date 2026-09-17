package io.github.heyhey123.xiaojieorm.impl.jdbc.database

import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.database.Transaction
import io.github.heyhey123.xiaojieorm.impl.jdbc.queries.JdbcQueries
import io.github.heyhey123.xiaojieorm.impl.jdbc.queries.PinnedConnectionSource
import io.github.heyhey123.xiaojieorm.queries.Queries
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
    timeout: Duration
) : Transaction(database, timeout) {

    /**
     * Statements run through a source that hands out the pinned connection and never closes it, so a
     * cursor that ends inside this transaction releases its result set and its statement and leaves the
     * connection alone.
     *
     * The statement timeout the source imposes is the transaction's own. A statement that hangs cannot
     * be interrupted by a rollback, because rolling back waits for the statement running on the same
     * connection, so the statement has to be the thing that gives up first.
     */
    override val queries: Queries = JdbcQueries(
        PinnedConnectionSource(connection, statementTimeoutSeconds = statementTimeoutSeconds(timeout)),
        dialect
    )

    override suspend fun doCommit() {
        connection.commit()
    }

    override suspend fun doRollback() {
        connection.rollback()
    }

    /**
     * Returns the connection to the pool in a state the next caller can use.
     *
     * Every step is attempted even when an earlier one fails, and the first failure is the one thrown,
     * the same way a cursor releases its resources. Leaving automatic commits off would hand a connection
     * to the next script with an open transaction on it, which is worse than the failure being reported.
     */
    override suspend fun doRelease() {
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

    private companion object {

        /** The transaction's own deadline, in whole seconds, as a floor of one. */
        fun statementTimeoutSeconds(timeout: Duration): Int =
            timeout.toSeconds().coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
    }
}
