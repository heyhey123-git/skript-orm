package io.github.heyhey123.skriptorm.impl.mongo.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import io.github.heyhey123.skriptorm.database.ConnectionSettings
import java.util.concurrent.TimeUnit

/**
 * Turns the properties of a `create a connection to database "MongoDB"` section into what the driver
 * wants.
 *
 * The url is read the way a JDBC url is read in the other implementations: `host:port` names the server,
 * a whole `mongodb://` connection string is taken as it is, and either may name the database in its path.
 * Credentials come from their own properties and are handed to the driver as strings, never pasted into
 * the url: a password containing `@`, `:` or `/` is a password, not a malformed connection string, and a
 * driver that receives it as an object has no reason to care what is in it.
 */
internal object MongoConnection {

    /** The database to use, when the url does not name one. */
    const val DATABASE_PROPERTY = "database"

    /** The database the credentials belong to, for a server whose users live somewhere other than the
     * database being used. Defaults to [DATABASE_PROPERTY], which is what the url form means too. */
    const val AUTH_DATABASE_PROPERTY = "auth database"

    /** What a connection without a declared database uses, so that one is always chosen. */
    const val DEFAULT_DATABASE = "skript-orm"

    /** The connection property a script uses to change how long one statement may take, in seconds. */
    const val STATEMENT_TIMEOUT_PROPERTY = "statement timeout"

    /** What a statement is given when the connection does not say. */
    const val DEFAULT_STATEMENT_TIMEOUT_SECONDS = 30

    private const val MONGODB_SCHEME = "mongodb://"
    private const val MONGODB_SRV_SCHEME = "mongodb+srv://"

    /**
     * Reads the statement timeout from the properties a `create a connection` body declared.
     *
     * The property is the one the JDBC implementations read and it means the same there, so a script that
     * writes `statement timeout: 15` gets fifteen seconds whichever database it connects to. Zero is a
     * value rather than a missing one: it leaves the driver's own setting alone, which is to wait.
     */
    fun statementTimeoutSeconds(properties: Map<String, String>): Int {
        val declared = properties[STATEMENT_TIMEOUT_PROPERTY]?.trim()
            ?: return DEFAULT_STATEMENT_TIMEOUT_SECONDS
        val seconds = declared.toIntOrNull() ?: throw IllegalArgumentException(
            "Connection property '$STATEMENT_TIMEOUT_PROPERTY' must be a whole number of seconds, " +
                "but was '$declared'."
        )
        require(seconds >= 0) {
            "Connection property '$STATEMENT_TIMEOUT_PROPERTY' must not be negative, but was $seconds."
        }
        return seconds
    }

    /**
     * The database this connection uses: the declared one, else the one the url names, else
     * [DEFAULT_DATABASE].
     */
    fun database(properties: Map<String, String>, url: String): String {
        properties[DATABASE_PROPERTY]?.takeIf { it.isNotEmpty() }?.let { return it }
        if (isConnectionString(url)) {
            ConnectionString(url).database?.takeIf { it.isNotEmpty() }?.let { return it }
        }
        return DEFAULT_DATABASE
    }

    /**
     * The client settings for [settings], with [database] as the database being used.
     *
     * A url that is already a connection string keeps everything it carries — options, and credentials if
     * it has them — and the username and password properties override its credentials when they are given.
     */
    fun clientSettings(
        settings: ConnectionSettings,
        database: String,
        properties: Map<String, String>
    ): MongoClientSettings = MongoClientSettings.builder().apply {
        if (isConnectionString(settings.url)) {
            applyConnectionString(ConnectionString(settings.url))
        } else {
            // A bare `host:port`, which is what a script that does not need connection options writes.
            // ServerAddress parses it and refuses anything malformed rather than connecting somewhere else.
            applyToClusterSettings { cluster -> cluster.hosts(listOf(ServerAddress(settings.url))) }
        }
        if (settings.username.isNotEmpty()) {
            credential(
                MongoCredential.createCredential(
                    settings.username,
                    authDatabase(properties, database),
                    settings.password.toCharArray()
                )
            )
        }
        // A statement waits on the socket its request went out on, so the socket read timeout is what
        // bounds it here, and the driver's default is to wait forever. The server keeps running a
        // statement the driver has given up on, exactly as it does for a JDBC statement the driver
        // cancelled: what this bounds is how long a script waits, not what the server does.
        //
        // This is applied whatever the url says, so a `socketTimeoutMS` in a connection string is
        // overridden by the property and by its default: one place to look is worth more than two.
        val timeoutSeconds = statementTimeoutSeconds(properties)
        if (timeoutSeconds > 0) {
            applyToSocketSettings { socket ->
                socket.readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            }
        }
    }.build()

    private fun authDatabase(properties: Map<String, String>, database: String): String =
        properties[AUTH_DATABASE_PROPERTY]?.takeIf { it.isNotEmpty() } ?: database

    private fun isConnectionString(url: String): Boolean =
        url.startsWith(MONGODB_SCHEME) || url.startsWith(MONGODB_SRV_SCHEME)
}
