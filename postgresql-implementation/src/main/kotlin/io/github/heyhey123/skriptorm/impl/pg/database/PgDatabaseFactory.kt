package io.github.heyhey123.skriptorm.impl.pg.database

import io.github.heyhey123.skriptorm.database.DatabaseFactory
import io.github.heyhey123.skriptorm.database.DatabaseRegistry
import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDatabase

object PgDatabaseFactory : DatabaseFactory {
    init {
        DatabaseRegistry.register(this)
    }

    override val typeName: String
        get() = "PostgreSQL"

    override fun create(properties: Map<String, String>) =
        PgDatabase(JdbcDatabase.statementTimeoutSeconds(properties))
}
