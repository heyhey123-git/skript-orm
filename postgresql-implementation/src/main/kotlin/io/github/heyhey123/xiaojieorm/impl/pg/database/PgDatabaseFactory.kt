package io.github.heyhey123.xiaojieorm.impl.pg.database

import io.github.heyhey123.xiaojieorm.database.DatabaseFactory

class PgDatabaseFactory: DatabaseFactory {

    override val typeName: String
        get() = "PostgreSQL"

    override fun create(properties: Map<String, String>) = PgDatabase()
}
