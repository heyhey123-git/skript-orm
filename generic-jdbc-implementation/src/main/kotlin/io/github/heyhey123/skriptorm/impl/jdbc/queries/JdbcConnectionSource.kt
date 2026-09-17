package io.github.heyhey123.skriptorm.impl.jdbc.queries

import java.sql.Connection
import javax.sql.DataSource
import kotlin.math.ceil

/**
 * Where a JDBC statement gets the connection it runs on, and how long it may take.
 *
 * Every statement borrows a connection and gives it back. Who owns it differs by how the work is
 * running: outside a transaction it comes from the pool and goes straight back, while inside one it is
 * the connection the transaction pinned, and giving that one back would end the transaction. The two
 * implementations differ in nothing else, which is why the choice lives here instead of in each of the
 * ten query classes.
 */
interface JdbcConnectionSource {

    /** Borrows a connection. The caller owes it to [release] when it is done with it. */
    fun borrow(): Connection

    /** Gives a borrowed connection back. */
    fun release(connection: Connection)

    /**
     * How long a statement run through this source may take, in seconds. Zero leaves the driver's
     * default, which is no limit at all.
     *
     * Asked once per statement rather than fixed when the source is made, because a source that knows a
     * deadline answers with what is left of it: a transaction's last statement must not be allowed to
     * outlive the transaction by a whole timeout.
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
 * One connection for as long as the source lives, which is what a transaction is.
 *
 * [release] does nothing on purpose: closing this connection is the transaction's decision, not a
 * statement's. A cursor that ends inside a transaction therefore releases its result set and its
 * statement and leaves the connection alone.
 *
 * The timeout is what is left until [deadlineNanos], rounded up to a whole second and never below one.
 * Rounding up matters at the end of a transaction: half a second left is still time a statement may
 * spend, and a statement that starts with nothing left gets one second to give up in, which is what
 * lets the watchdog's rollback proceed instead of waiting on a statement with no bound at all.
 */
class PinnedConnectionSource(
    private val connection: Connection,
    private val deadlineNanos: Long
) : JdbcConnectionSource {

    override fun borrow(): Connection = connection

    override fun release(connection: Connection) = Unit

    override fun statementTimeoutSeconds(): Int {
        val remainingSeconds = (deadlineNanos - System.nanoTime()) / NANOS_PER_SECOND
        return ceil(remainingSeconds).coerceAtLeast(1.0).toInt()
    }

    private companion object {
        const val NANOS_PER_SECOND = 1_000_000_000.0
    }
}
