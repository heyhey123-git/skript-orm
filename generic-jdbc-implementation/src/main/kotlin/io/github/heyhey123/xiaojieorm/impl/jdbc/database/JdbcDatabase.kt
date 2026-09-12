package io.github.heyhey123.xiaojieorm.impl.jdbc.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.impl.jdbc.queries.JdbcQueries
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataTypes
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.DataTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class JdbcDatabase(
    val driver: String
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

            queries = JdbcQueries(dataSource!!)
            DataTypes.INSTANCE = JdbcDataTypes
        } catch (e: ClassNotFoundException) {
            throw ClassNotFoundException("JDBC Driver class not found: $driver", e)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to connect to the database: $url", e)
        }
    }

    override fun doDisconnect() {
        dataSource?.close()
        dataSource = null
    }

    override fun doRegisterTable(table: Table): Job {
        check(dataSource != null) { "Database is not connected. Please connect before registering tables." }
        val sql = buildString {
            append("CREATE TABLE IF NOT EXISTS ${table.name} (")

            table.columns.values.forEachIndexed { index, column ->
                val columnType = column.type as JdbcDataType<*>
                append("${column.name} ${columnType.storageName}")
                val size = if (column.size != null) column.size!!
                    else columnType.defaultSize
                if (size >= 0) {
                    append("($size)")
                }

                if (!column.isNullable) append(" NOT NULL")
                if (column.isPrimaryKey) append(" PRIMARY KEY")
                if (column.isAutoIncrement) append(" AUTO_INCREMENT")

                if (index < table.columns.size - 1) append(", ")
            }

            append(");")
        }

        return CoroutineScope(Dispatchers.IO).launch {
            dataSource!!.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeUpdate(sql)
                }
            }
        }
    }

}
