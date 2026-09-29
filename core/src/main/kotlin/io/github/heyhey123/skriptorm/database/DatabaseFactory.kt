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
     * Every connection property name this implementation reads.
     *
     * A `create a connection` body may declare any name at all, and an implementation that reads only the
     * ones it knows used to drop the rest without a word: `database: "sicilia_db"` on a MySQL connection
     * looked exactly like a connection that had been given its database, while the statements went to a
     * connection with none selected. The set is therefore the contract, and
     * [io.github.heyhey123.skriptorm.skript.utils.ConnectionPropertiesParser] is where it is enforced.
     *
     * [ConnectionProperties.URL], [ConnectionProperties.USERNAME] and [ConnectionProperties.PASSWORD] are
     * always accepted and need not be listed: they are how a connection is opened, not something an
     * implementation opts into.
     */
    val acceptedConnectionProperties: Set<String>

    /**
     * Why this implementation cannot honour [name], or null when it can.
     *
     * The default is null, which leaves the refusal to the caller and produces the generic
     * "not read by this implementation" message listing [acceptedConnectionProperties]. An
     * implementation overrides this only when it has something more useful to say, which is the case for
     * a property that belongs to another implementation and has a replacement here.
     */
    fun describeRejectedProperty(name: String): String? = null

    /**
     * Create a new instance of the Database.
     * @return A new Database instance.
     */
    fun create(properties: Map<String, String>): Database
}

/**
 * The property names every implementation reads, so that a script never has to spell them out: they are
 * how a connection is opened, and [DatabaseFactory.acceptedConnectionProperties] adds what each
 * implementation reads on top.
 */
object ConnectionProperties {

    const val URL = "url"
    const val USERNAME = "username"
    const val PASSWORD = "password"

    /** The three above, in the order they are named in a refusal. */
    val ALWAYS_READ: Set<String> = linkedSetOf(URL, USERNAME, PASSWORD)
}
