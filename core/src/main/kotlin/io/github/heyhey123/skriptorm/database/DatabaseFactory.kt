package io.github.heyhey123.skriptorm.database

/**
 * Creates unconnected database instances from implementation-specific properties.
 * Factories become discoverable after registration in [DatabaseRegistry].
 */
interface DatabaseFactory {

    /**
     * The name of the database type, e.g., "PostgreSQL", "MySQL", etc.
     */
    val typeName: String

    /**
     * Create a new instance of the Database.
     * @return A new Database instance.
     */
    fun create(properties: Map<String, String>): Database
}
