package io.github.heyhey123.xiaojieorm.impl.jdbc.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.impl.jdbc.queries.JdbcQueries
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataTypes
import io.github.heyhey123.xiaojieorm.table.Table

open class JdbcDatabase(
    val driver: String,
    val dialect: JdbcDialect
) : Database() {

    var dataSource: HikariDataSource? = null

    override val dataTypes: JdbcDataTypes = JdbcDataTypes

    override fun doConnect(url: String, user: String, password: String) {
        try {
            val config = HikariConfig().apply {
                jdbcUrl = url
                username = user
                this.password = password
                driverClassName = driver
            }
            dataSource = HikariDataSource(config)

            queries = JdbcQueries(dataSource!!, dialect)
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
            throw IllegalStateException("Failed to connect to the database: $url", e)
        }
    }

    override fun doDisconnect() {
        dataSource?.close()
        dataSource = null
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
