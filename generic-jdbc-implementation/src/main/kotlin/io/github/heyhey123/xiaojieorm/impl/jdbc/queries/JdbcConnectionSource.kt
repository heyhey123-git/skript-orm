package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import java.sql.Connection
import javax.sql.DataSource

/**
 * Where a JDBC statement gets the connection it runs on.
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
     * Statement timeout, in seconds, that statements running through this source have to respect.
     * Zero imposes nothing, leaving the query's own value and then the driver's default.
     */
    val statementTimeoutSeconds: Int
        get() = 0
}

/** One connection per statement, borrowed from a pool and returned to it. */
class PooledConnectionSource(
    private val dataSource: DataSource,
    override val statementTimeoutSeconds: Int = 0
) : JdbcConnectionSource {

    override fun borrow(): Connection = dataSource.connection

    override fun release(connection: Connection) {
        connection.close()
    }
}

/**
 * One connection for as long as the source lives, which is what a transaction is.
 *
 * [release] does nothing on purpose: closing this connection is the transaction's decision, not a
 * statement's. A cursor that ends inside a transaction therefore releases its result set and its
 * statement and leaves the connection alone.
 */
class PinnedConnectionSource(
    private val connection: Connection,
    override val statementTimeoutSeconds: Int = 0
) : JdbcConnectionSource {

    override fun borrow(): Connection = connection

    override fun release(connection: Connection) = Unit
}
