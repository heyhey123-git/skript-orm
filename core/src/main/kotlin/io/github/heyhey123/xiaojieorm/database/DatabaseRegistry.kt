package io.github.heyhey123.xiaojieorm.database

/**
 * Database types registry.
 * Allows registration and retrieval of different database types.
 */
object DatabaseRegistry {

    /**
     * Registered database factories.
     */
    private val factories = mutableMapOf<String, DatabaseFactory>()

    /**
     * Register a database factory.
     *
     * @param factory The database factory to register.
     */
    fun register(factory: DatabaseFactory) {
        factories[factory.typeName] = factory
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
            ?: error("Unsupported database type: $typeName")
        return factory.create(properties)
    }

    /**
     * Check if a database type is supported.
     *
     * @param typeName The type name of the database.
     * @return True if the database type is supported, false otherwise.
     */
    fun isSupported(typeName: String): Boolean = typeName in factories
}
