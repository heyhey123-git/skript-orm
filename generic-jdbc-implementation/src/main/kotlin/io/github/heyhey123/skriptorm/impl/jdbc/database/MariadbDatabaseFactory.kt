package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.database.DatabaseFactory
import io.github.heyhey123.skriptorm.database.DatabaseRegistry

/**
 * The `"MariaDB"` type: [MysqlJdbcDialect] with MariaDB Connector/J.
 *
 * A type of its own rather than a fallback inside `"MySQL"`, because the second driver is the point.
 * MariaDB Connector/J refuses a `jdbc:mysql://` URL, MySQL Connector/J accepts it, so trying one and
 * falling back to the other would reach a MariaDB server through the MySQL driver without saying so —
 * which is the slow batch write this type exists to avoid. A URL whose scheme does not match its type
 * fails at connect instead.
 *
 * The driver name is read before the registration below, so a server that never downloaded the driver
 * reports a type that failed to load rather than one that is listed and cannot connect.
 */
object MariadbDatabaseFactory : DatabaseFactory {

    val driverName = run {
        try {
            Class.forName("org.mariadb.jdbc.Driver")
            "org.mariadb.jdbc.Driver"
        } catch (e: ClassNotFoundException) {
            throw ClassNotFoundException("MariaDB JDBC Driver not found. Please include the MariaDB Connector/J library in your classpath.").initCause(e)
        }
    }

    init {
        DatabaseRegistry.register(this)
    }

    override val typeName: String
        get() = "MariaDB"

    override val acceptedConnectionProperties: Set<String> = setOf(JdbcDatabase.STATEMENT_TIMEOUT_PROPERTY)

    override fun create(properties: Map<String, String>) = JdbcDatabase(
        driverName,
        MysqlJdbcDialect,
        JdbcDatabase.statementTimeoutSeconds(properties),
        typeName
    )
}
