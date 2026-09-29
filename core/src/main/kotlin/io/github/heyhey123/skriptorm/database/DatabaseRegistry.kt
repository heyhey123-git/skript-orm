package io.github.heyhey123.skriptorm.database

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
     * A factory registers itself as its object is initialized, so [DatabaseFactory.typeName] is read while
     * that object is still being built: an implementation whose name lives in a property has to register
     * itself after that property is assigned, which is why the factories of this repository compute it.
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
    fun get(typeName: String, properties: Map<String, String>): Database =
        factory(typeName).create(properties)

    /**
     * The implementation registered under [typeName].
     *
     * Read on its own by the callers that have to ask the implementation something before it exists,
     * such as which connection properties it reads: creating an instance to find that out would open a
     * connection, and the property is exactly what the connection must not be opened with.
     *
     * @throws IllegalArgumentException if the database type is unsupported.
     */
    fun factory(typeName: String): DatabaseFactory = factories[typeName]
        ?: throw IllegalArgumentException("Unsupported database type: $typeName")

    /**
     * Check if a database type is supported.
     *
     * @param typeName The type name of the database.
     * @return True if the database type is supported, false otherwise.
     */
    fun isSupported(typeName: String): Boolean = typeName in factories.keys

    /**
     * Every registered implementation, in no particular order.
     *
     * Read by the startup report, which says what a server actually has: the names here are the ones a
     * script writes after `database`, so this is what a server owner needs when a type they expected is
     * missing.
     */
    fun registered(): Collection<DatabaseFactory> = factories.values.toList()
}
