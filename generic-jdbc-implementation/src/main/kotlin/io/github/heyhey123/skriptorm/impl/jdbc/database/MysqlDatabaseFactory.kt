package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.database.DatabaseFactory
import io.github.heyhey123.skriptorm.database.DatabaseRegistry

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

    override val acceptedConnectionProperties: Set<String> = setOf(JdbcDatabase.STATEMENT_TIMEOUT_PROPERTY)

    /**
     * A body written for MongoDB is the mistake this is most likely to be met with, so it is answered
     * with what to write instead. The property was passed to the implementation and dropped before, which
     * left the connection without a default database and failed later on the first statement that needed
     * one, naming neither the property nor the connection.
     */
    override fun describeRejectedProperty(name: String): String? =
        if (name == "database" || name == "auth database") {
            "Connection property '$name' is not read by database 'MySQL': MySQL takes its database from " +
                "the url path, as in \"jdbc:mysql://localhost:3306/sicilia_db\". " +
                "'$name' is a MongoDB connection property and was ignored before this check existed."
        } else {
            null
        }

    override fun create(properties: Map<String, String>) = JdbcDatabase(
        driverName,
        MysqlJdbcDialect,
        JdbcDatabase.statementTimeoutSeconds(properties)
    )
}
