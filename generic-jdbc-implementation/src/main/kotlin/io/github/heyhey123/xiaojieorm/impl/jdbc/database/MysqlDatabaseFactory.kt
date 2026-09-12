package io.github.heyhey123.xiaojieorm.impl.jdbc.database

import io.github.heyhey123.xiaojieorm.database.DatabaseFactory
import io.github.heyhey123.xiaojieorm.database.DatabaseRegistry

object MysqlDatabaseFactory : DatabaseFactory {
    init {
        DatabaseRegistry.register(this)
    }

    val driverName = run {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            "com.mysql.cj.jdbc.Driver"
        } catch (e: ClassNotFoundException) {
            try {
                Class.forName("com.mysql.jdbc.Driver")
                "com.mysql.jdbc.Driver"
            } catch (e: ClassNotFoundException) {
                throw ClassNotFoundException("MySQL JDBC Driver not found. Please include the MySQL Connector/J library in your classpath.").initCause(e)
            }
        }
    }

    override val typeName: String
        get() = "MySQL"

    override fun create(properties: Map<String, String>) =
        JdbcDatabase(driverName)
}
