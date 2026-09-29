package io.github.heyhey123.skriptorm.impl.pg.database

import io.github.heyhey123.skriptorm.database.DatabaseFactory
import io.github.heyhey123.skriptorm.database.DatabaseRegistry
import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDatabase

object PgDatabaseFactory : DatabaseFactory {
    init {
        DatabaseRegistry.register(this)
    }

    override val typeName: String
        get() = "PostgreSQL"

    override val acceptedConnectionProperties: Set<String> = setOf(JdbcDatabase.STATEMENT_TIMEOUT_PROPERTY)

    /**
     * The two property names a script is most likely to bring over from another implementation, answered
     * with where the database belongs instead. PostgreSQL has no `auth database` equivalent here: its
     * roles are not scoped to a database the way MongoDB's are.
     */
    override fun describeRejectedProperty(name: String): String? =
        if (name == "database" || name == "auth database") {
            "Connection property '$name' is not read by database 'PostgreSQL': PostgreSQL takes its " +
                "database from the url path, as in \"jdbc:postgresql://localhost:5432/mydb\". " +
                "'$name' is a MongoDB connection property and was ignored before this check existed."
        } else {
            null
        }

    override fun create(properties: Map<String, String>) =
        PgDatabase(JdbcDatabase.statementTimeoutSeconds(properties))
}
