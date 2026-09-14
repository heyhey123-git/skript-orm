package io.github.heyhey123.xiaojieorm.impl.jdbc.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.impl.jdbc.queries.JdbcQueries
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataTypes
import io.github.heyhey123.xiaojieorm.table.Table

open class JdbcDatabase(
    val driver: String,
    val dialect: JdbcDialect = GenericJdbcDialect
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
        val sql = buildString {
            append("CREATE TABLE IF NOT EXISTS ${table.name} (")

            table.columns.values.forEachIndexed { index, column ->
                val columnType = requireNotNull(column.type as? JdbcDataType<*>) {
                    "Data type ${column.type.typeCode} is not supported by the JDBC implementation."
                }
                append("${column.name} ${columnType.storageName}")
                val size = column.size ?: columnType.defaultSize.takeIf { it >= 0 }
                if (size != null) {
                    require(columnType.supportsSize) {
                        "JDBC type ${columnType.storageName} does not support a column size."
                    }
                    append("($size)")
                }

                if (!column.isNullable) append(" NOT NULL")
                if (column.isPrimaryKey) append(" PRIMARY KEY")
                if (column.isAutoIncrement) {
                    require(columnType.jdbcType in setOf(
                        java.sql.JDBCType.TINYINT,
                        java.sql.JDBCType.SMALLINT,
                        java.sql.JDBCType.INTEGER,
                        java.sql.JDBCType.BIGINT
                    )) { "Auto-increment column ${column.name} must use an integer JDBC type." }
                    append(dialect.autoIncrementClause())
                }

                if (index < table.columns.size - 1) append(", ")
            }

            append(");")
        }

        source.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(sql)
            }
        }
    }

}
