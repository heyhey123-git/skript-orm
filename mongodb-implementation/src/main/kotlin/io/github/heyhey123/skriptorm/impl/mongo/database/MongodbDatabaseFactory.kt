package io.github.heyhey123.skriptorm.impl.mongo.database

import io.github.heyhey123.skriptorm.database.DatabaseFactory
import io.github.heyhey123.skriptorm.database.DatabaseRegistry

object MongodbDatabaseFactory : DatabaseFactory {

    init {
        DatabaseRegistry.register(this)
    }

    /**
     * The name a script writes after `database`. Computed rather than stored, because [DatabaseRegistry]
     * reads it while this object is being initialized and an initializer that runs after the `init` block
     * above would still be null at that point.
     */
    override val typeName: String
        get() = "MongoDB"

    override fun create(properties: Map<String, String>) = MongodbDatabase(properties)
}
