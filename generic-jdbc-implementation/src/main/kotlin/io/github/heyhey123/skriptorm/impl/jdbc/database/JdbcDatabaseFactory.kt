package io.github.heyhey123.skriptorm.impl.jdbc.database

import io.github.heyhey123.skriptorm.database.DatabaseFactory
import io.github.heyhey123.skriptorm.database.DatabaseRegistry

object JdbcDatabaseFactory : DatabaseFactory {

    init {
        DatabaseRegistry.register(this)
    }

    override val typeName: String
        get() = "JDBC"

    override fun create(properties: Map<String, String>): JdbcDatabase {
        val driver = properties["driver"]
            ?: throw IllegalArgumentException("JDBC driver class name must be provided in properties with key 'driver'")
        return JdbcDatabase(driver, GenericJdbcDialect, JdbcDatabase.statementTimeoutSeconds(properties))
    }
}
