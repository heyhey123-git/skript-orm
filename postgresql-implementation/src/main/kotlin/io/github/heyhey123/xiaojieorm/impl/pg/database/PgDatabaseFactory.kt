package io.github.heyhey123.xiaojieorm.impl.pg.database

import io.github.heyhey123.xiaojieorm.database.DatabaseFactory
import io.github.heyhey123.xiaojieorm.database.DatabaseRegistry
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabase

object PgDatabaseFactory : DatabaseFactory {
    init {
        DatabaseRegistry.register(this)
    }

    override val typeName: String
        get() = "PostgreSQL"

    override fun create(properties: Map<String, String>) =
        PgDatabase(JdbcDatabase.statementTimeoutSeconds(properties))
}
