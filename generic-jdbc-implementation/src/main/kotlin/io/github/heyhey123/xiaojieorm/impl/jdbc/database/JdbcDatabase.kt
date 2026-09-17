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
 * Hikari-backed JDBC database configured with a driver class and [dialect].
 *
 * Connecting creates the pool and query factory. Disconnecting closes the pool. Failed connection
 * attempts close any partially created pool and clear JDBC state. Table registration executes the
 * dialect's `CREATE TABLE IF NOT EXISTS` statement using a borrowed connection.
 */
open class JdbcDatabase(
    val driver: String,
    val dialect: JdbcDialect
) : Database() {

    var dataSource: HikariDataSource? = null

    override val dataTypes: DataTypes = JdbcDataTypes

    override fun doConnect(settings: ConnectionSettings) {
        try {
            val config = HikariConfig().apply {
                jdbcUrl = settings.url
                username = settings.username
                password = settings.password
                driverClassName = driver
            }
            dataSource = HikariDataSource(config)

            queries = JdbcQueries(PooledConnectionSource(dataSource!!), dialect)
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

    override suspend fun doRegisterTable(table: Table) {
        val source = checkNotNull(dataSource) {
            "Database is not connected. Please connect before registering tables."
        }
        val sql = dialect.createTable(table)

        source.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(sql)
            }
        }
    }
}
