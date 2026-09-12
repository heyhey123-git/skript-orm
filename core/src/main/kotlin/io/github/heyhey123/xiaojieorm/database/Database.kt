package io.github.heyhey123.xiaojieorm.database

import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.DataTypes
import kotlinx.coroutines.Job
import kotlinx.coroutines.future.asCompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a generic database connection and operations.
 *
 */
abstract class Database {
    companion object {
        /**
         * The currently active database connection.
         */
        var current: Database? = null

        /**
         * The set of registered tables in the current database.
         */
        val tables: MutableMap<String, Table> = ConcurrentHashMap()
    }

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
     * Establishes a connection to the database using the provided URL, username, and password.
     *
     * @param url The database connection URL.
     * @param user The username for authentication.
     * @param password The password for authentication.
     */
    fun connect(url: String, user: String, password: String) {
        doConnect(url, user, password)
        current = this
        if (tables.isNotEmpty()) {
            tables.forEach { (_, table) ->
                doRegisterTable(table)
            }
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
     *
     */
    fun disconnect() {
        isConnected = false
        queries = null
        doDisconnect()
        current = null
        tables.clear()
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
    fun registerTable(table: Table) {
        doRegisterTable(table).asCompletableFuture().thenAccept {
            tables[table.name] = table
        }
    }

    /**
     * Performs the actual table registration logic specific to the database implementation.
     *
     * @param table The table to register.
     * @return A Job representing the asynchronous registration operation.
     */
    abstract fun doRegisterTable(table: Table): Job
}
