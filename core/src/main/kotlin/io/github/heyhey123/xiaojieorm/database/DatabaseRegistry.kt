package io.github.heyhey123.xiaojieorm.database

import java.util.concurrent.ConcurrentHashMap

/**
 * Database types registry.
 * Allows registration and retrieval of different database types.
 */
object DatabaseRegistry {

    /**
     * Registered database factories.
     */
    private val factories = ConcurrentHashMap<String, DatabaseFactory>()

    /**
     * Register a database factory.
     *
     * @param factory The database factory to register.
     */
    fun register(factory: DatabaseFactory) {
        val existing = factories.putIfAbsent(factory.typeName, factory)
        require(existing == null || existing === factory) {
            "Database type '${factory.typeName}' is already registered by ${existing!!::class.java.name}."
        }
    }

    /**
     * Get a database instance by type name and properties.
     *
     * @param typeName The type name of the database.
     * @param properties The properties for creating the database instance.
     * @return The created database instance.
     * @throws IllegalArgumentException if the database type is unsupported.
     */
    fun get(typeName: String, properties: Map<String, String>): Database {
        val factory = factories[typeName]
            ?: throw IllegalArgumentException("Unsupported database type: $typeName")
        return factory.create(properties)
    }

    /**
     * Check if a database type is supported.
     *
     * @param typeName The type name of the database.
     * @return True if the database type is supported, false otherwise.
     */
    fun isSupported(typeName: String): Boolean = typeName in factories.keys
}
