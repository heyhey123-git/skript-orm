package io.github.heyhey123.xiaojieorm.database

/**
 * Database factory interface.
 * The classes implementing this interface in this project provide a static block to register themselves,
 * so that we can register different database types by calling [Class.forName] on their class names.
 *
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
