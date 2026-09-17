package io.github.heyhey123.xiaojieorm.impl.jdbc.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.heyhey123.xiaojieorm.database.ConnectionSettings
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.database.Transaction
import io.github.heyhey123.xiaojieorm.impl.jdbc.queries.JdbcQueries
import io.github.heyhey123.xiaojieorm.impl.jdbc.queries.PooledConnectionSource
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataTypes
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.DataTypes
import java.time.Duration

/**
 * Hikari-backed JDBC database configured with a driver class, [dialect], and a statement timeout.
 *
 * Connecting creates the pool and query factory. Disconnecting closes the pool. Failed connection
 * attempts close any partially created pool and clear JDBC state. Table registration executes the
 * dialect's `CREATE TABLE IF NOT EXISTS` statement using a borrowed connection.
 */
open class JdbcDatabase(
    val driver: String,
    val dialect: JdbcDialect,
    /** How long one statement may take, in seconds. Zero leaves the driver's default, which is none. */
    val statementTimeoutSeconds: Int = DEFAULT_STATEMENT_TIMEOUT_SECONDS
) : Database() {

    companion object {

        /** The connection property a script uses to change the statement timeout, in seconds. */
        const val STATEMENT_TIMEOUT_PROPERTY = "statement timeout"

        /**
         * How long a statement gets when the script says nothing.
         *
         * Long enough that honest work does not meet it, short enough that a statement nothing can
         * interrupt does not hold one of the pool's connections until the server restarts. The same
         * number as the transaction default, so there is one duration to remember rather than two.
         */
        const val DEFAULT_STATEMENT_TIMEOUT_SECONDS = 30

        /** How much longer a disconnect waits than a statement may take, so that a timeout wins. */
        private const val DRAIN_MARGIN_SECONDS = 5L

        /**
         * Reads the statement timeout from the properties a `create a connection` body declared.
         *
         * Zero is a value, not a missing one: it means "leave this to the driver", which is what a script
         * with a legitimately slow statement wants.
         */
        fun statementTimeoutSeconds(properties: Map<String, String>): Int {
            val declared = properties[STATEMENT_TIMEOUT_PROPERTY]?.trim()
                ?: return DEFAULT_STATEMENT_TIMEOUT_SECONDS
            val seconds = declared.toIntOrNull() ?: throw IllegalArgumentException(
                "Connection property '$STATEMENT_TIMEOUT_PROPERTY' must be a whole number of seconds, " +
                    "but was '$declared'."
            )
            require(seconds >= 0) {
                "Connection property '$STATEMENT_TIMEOUT_PROPERTY' must not be negative, but was $seconds."
            }
            return seconds
        }
    }

    var dataSource: HikariDataSource? = null

    override val dataTypes: DataTypes = JdbcDataTypes

    /**
     * Longer than a statement may take, so that a statement which is about to time out on its own does:
     * it fails, releases its lease, and the disconnect it interrupted goes on to close the pool as usual.
     */
    override val drainTimeout: Duration
        get() = if (statementTimeoutSeconds > 0) {
            Duration.ofSeconds(statementTimeoutSeconds.toLong() + DRAIN_MARGIN_SECONDS)
        } else {
            DEFAULT_DRAIN_TIMEOUT
        }

    override fun doConnect(settings: ConnectionSettings) {
        try {
            val config = HikariConfig().apply {
                jdbcUrl = settings.url
                username = settings.username
                password = settings.password
                driverClassName = driver
            }
            dataSource = HikariDataSource(config)

            queries = JdbcQueries(PooledConnectionSource(dataSource!!, statementTimeoutSeconds), dialect)
        } catch (e: ClassNotFoundException) {
            throw ClassNotFoundException("JDBC Driver class not found: $driver", e)
        } catch (e: Exception) {
            try {
                dataSource?.close()
            } catch (cleanupError: Throwable) {
                e.addSuppressed(cleanupError)
            }
            dataSource = null
            queries = null
            throw IllegalStateException("Failed to connect to the database: ${settings.url}", e)
        }
    }

    override fun doDisconnect() {
        dataSource?.close()
        dataSource = null
    }

    override val supportsTransactions: Boolean = true

    /**
     * Borrows one connection and turns automatic commits off on it.
     *
     * The connection stays out of the pool until the transaction ends, which is the whole point: the
     * statements inside it have to run on the same one to see each other's work.
     *
     * A connection that cannot be taken out of automatic-commit mode is given straight back, so that a
     * driver which refuses does not cost the pool a connection for the life of the server.
     */
    override fun doBeginTransaction(timeout: Duration): Transaction {
        val source = checkNotNull(dataSource) {
            "Database is not connected. Please connect before opening a transaction."
        }
        val connection = source.connection
        try {
            connection.autoCommit = false
        } catch (error: Throwable) {
            try {
                connection.close()
            } catch (cleanupError: Throwable) {
                error.addSuppressed(cleanupError)
            }
            throw error
        }
        return JdbcTransaction(this, dialect, connection, timeout)
    }

    /**
     * Creates the table, with the same statement timeout every other statement gets.
     *
     * Leaving this one out would make "30 seconds unless you say otherwise" untrue of the statements
     * most likely to wait: MySQL bounds a `CREATE TABLE` waiting on a metadata lock by
     * `lock_wait_timeout`, whose default is a year.
     */
    override suspend fun doRegisterTable(table: Table) {
        val source = checkNotNull(dataSource) {
            "Database is not connected. Please connect before registering tables."
        }
        val sql = dialect.createTable(table)

        source.connection.use { connection ->
            connection.createStatement().use { statement ->
                if (statementTimeoutSeconds > 0) statement.queryTimeout = statementTimeoutSeconds
                statement.executeUpdate(sql)
            }
        }
    }
}
