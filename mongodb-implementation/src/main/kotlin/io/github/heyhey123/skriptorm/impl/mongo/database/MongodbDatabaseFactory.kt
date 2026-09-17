package io.github.heyhey123.skriptorm.impl.mongo.database

import io.github.heyhey123.skriptorm.database.DatabaseFactory
import io.github.heyhey123.skriptorm.database.DatabaseRegistry

object MongodbDatabaseFactory : DatabaseFactory {

    init {
        DatabaseRegistry.register(this)
    }

    override val typeName: String = "MongoDB"

    override fun create(properties: Map<String, String>) = MongodbDatabase()
}
