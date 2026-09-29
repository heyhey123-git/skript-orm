package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.database.DatabaseFactory
import io.github.heyhey123.skriptorm.database.DatabaseRegistry

object JdbcDatabaseFactory : DatabaseFactory {

    init {
        DatabaseRegistry.register(this)
    }

    override val typeName: String
        get() = "JDBC"

    /** The driver class name this type cannot work without, plus the shared statement timeout. */
    override val acceptedConnectionProperties: Set<String> = setOf(DRIVER_PROPERTY, JdbcDatabase.STATEMENT_TIMEOUT_PROPERTY)

    override fun create(properties: Map<String, String>): JdbcDatabase {
        val driver = properties[DRIVER_PROPERTY]
            ?: throw IllegalArgumentException(
                "Database 'JDBC' requires the connection property '$DRIVER_PROPERTY', naming the driver " +
                    "class to use, for example \"org.sqlite.JDBC\"."
            )
        return JdbcDatabase(driver, GenericJdbcDialect, JdbcDatabase.statementTimeoutSeconds(properties), typeName)
    }

    /** The property that names the driver class for this type. */
    const val DRIVER_PROPERTY: String = "driver"
}
