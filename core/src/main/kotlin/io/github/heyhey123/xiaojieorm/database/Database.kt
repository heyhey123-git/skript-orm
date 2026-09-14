package io.github.heyhey123.xiaojieorm.database

import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.DataTypes
import kotlinx.coroutines.Job
import org.bukkit.Bukkit
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a generic database connection and operations.
 *
 */
abstract class Database {
    companion object {

        var current: Database? = null
    }

    /**
     * Tables registered for this database instance.
     */
    val tables: MutableMap<String, Table> = ConcurrentHashMap()

    /**
     * Indicates whether the database is currently connected.
     */
    var isConnected: Boolean = false
        protected set

    /**
     * The queries specific to the database implementation.
     */
    var queries: Queries? = null

    /**
     * The data types supported by the database.
     */
    abstract val dataTypes: DataTypes


    /**
     * Connects to the database using the provided URL, username, and password.
     * This method must be called on the primary thread and ensures that only one database connection is active at a time.
     *
     * @throws error if the connection fails or if another database is already connected.
     */
    fun connect(url: String, user: String, password: String) {
        check(Bukkit.isPrimaryThread()) { "Connection must be established on the primary thread." }
        check(!isConnected) { "Database is already connected." }
        check(current == null || current === this) {
            "Another database is already connected. Disconnect it before connecting a new database."
        }

        try {
            doConnect(url, user, password)
            check(queries != null) { "Database implementation did not initialize queries during connection." }
            isConnected = true
            current = this
            if (tables.isNotEmpty()) {
                tables.forEach { (_, table) -> doRegisterTable(table) }
            }
        } catch (error: Throwable) {
            try {
                doDisconnect()
            } catch (cleanupError: Throwable) {
                error.addSuppressed(cleanupError)
            }
            queries = null
            isConnected = false
            if (current === this) current = null
            throw error
        }
    }

    /**
     * Performs the actual connection logic specific to the database implementation.
     * The field [queries] should be initialized in this method.
     *
     * @param url The database connection URL.
     * @param user The username for authentication.
     * @param password The password for authentication.
     */
    protected abstract fun doConnect(url: String, user: String, password: String)

    /**
     * Disconnects from the database.
     * If there is no active connection, this method does nothing.
     * This method must be called on the primary thread.
     * @throws error if the disconnection fails.
     */
    fun disconnect() {
        check(Bukkit.isPrimaryThread()) { "Disconnection must be performed on the primary thread." }
        if (!isConnected && queries == null && current !== this) return
        var failure: Throwable? = null
        try {
            doDisconnect()
        } catch (error: Throwable) {
            failure = error
        } finally {
            isConnected = false
            queries = null
            if (current === this) {
                current = null
            }
        }
        failure?.let { throw it }
    }

    /**
     * Performs the actual disconnection logic specific to the database implementation.
     *
     */
    protected abstract fun doDisconnect()

    /**
     * Registers a table in the database.
     *
     * @param table The table to register.
     */
    fun registerTable(table: Table): Job {
        check(isConnected) { "Database is not connected." }
        val registration = doRegisterTable(table)
        registration.invokeOnCompletion { error ->
            if (error == null) tables[table.name] = table
        }
        return registration
    }

    /**
     * Performs the actual table registration logic specific to the database implementation.
     *
     * @param table The table to register.
     * @return A Job representing the asynchronous registration operation.
     */
    abstract fun doRegisterTable(table: Table): Job
}
