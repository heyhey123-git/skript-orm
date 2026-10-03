package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.database.DatabaseFactory
import io.github.heyhey123.skriptorm.database.DatabaseRegistry

object MysqlDatabaseFactory : DatabaseFactory {

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

    init {
        DatabaseRegistry.register(this)
    }

    override val typeName: String
        get() = "MySQL"

    override val acceptedConnectionProperties: Set<String> = setOf(JdbcDatabase.STATEMENT_TIMEOUT_PROPERTY)

    override fun create(properties: Map<String, String>) = JdbcDatabase(
        driverName,
        MysqlServerJdbcDialect,
        JdbcDatabase.statementTimeoutSeconds(properties),
        typeName
    )
}
