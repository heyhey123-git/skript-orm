package io.github.heyhey123.xiaojieorm.database

import io.github.heyhey123.xiaojieorm.database.Database.Companion.current
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.DataTypes
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a generic database connection and operations.
 */
abstract class Database {
    companion object {
        private val lifecycleMutex = Mutex()

        /** The currently active, fully initialized database connection. */
        @Volatile
        var current: Database? = null
            private set
    }

    enum class State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    /** Tables successfully registered for this database instance. */
    val tables: MutableMap<String, Table> = ConcurrentHashMap()

    @Volatile
    var state: State = State.DISCONNECTED
        private set

    val isConnected: Boolean
        get() = state == State.CONNECTED

    /** Queries specific to the database implementation. */
    @Volatile
    var queries: Queries? = null
        protected set

    /** Data types supported by this database. */
    abstract val dataTypes: DataTypes

    /**
     * Establishes and fully initializes this database.
     * The instance is published through [current] only after all previously registered tables have
     * been recreated successfully.
     */
    suspend fun connect(url: String, user: String, password: String) = lifecycleMutex.withLock {
        check(state == State.DISCONNECTED) { "Database is not disconnected: $state." }
        check(current == null || current === this) {
            "Another database is already connected. Disconnect it before connecting a new database."
        }

        state = State.CONNECTING
        try {
            doConnect(url, user, password)
            check(queries != null) { "Database implementation did not initialize queries during connection." }

            for (table in tables.values.toList()) {
                doRegisterTable(table)
            }

            state = State.CONNECTED
            current = this
        } catch (error: Throwable) {
            try {
                doDisconnect()
            } catch (cleanupError: Throwable) {
                error.addSuppressed(cleanupError)
            }
            queries = null
            state = State.DISCONNECTED
            if (current === this) current = null
            throw error
        }
    }

    /** Initializes implementation-specific resources and [queries]. */
    protected abstract fun doConnect(url: String, user: String, password: String)

    /**
     * Disconnects this database. State is cleared even when implementation cleanup fails.
     */
    suspend fun disconnect() = lifecycleMutex.withLock {
        if (state == State.DISCONNECTED && queries == null && current !== this) return@withLock

        state = State.DISCONNECTING
        var failure: Throwable? = null
        try {
            doDisconnect()
        } catch (error: Throwable) {
            failure = error
        } finally {
            queries = null
            state = State.DISCONNECTED
            if (current === this) current = null
        }
        failure?.let { throw it }
    }

    /** Releases implementation-specific resources. */
    protected abstract fun doDisconnect()

    /**
     * Registers a table and publishes it to [tables] only after registration succeeds.
     */
    suspend fun registerTable(table: Table) = lifecycleMutex.withLock {
        check(state == State.CONNECTED) { "Database is not connected." }
        doRegisterTable(table)
        tables[table.name] = table
    }

    /**
     * Structured registration hook for implementations. New implementations should override this.
     * The default bridge keeps existing Job-based implementations source-compatible while ensuring
     * their completion and failure are awaited by the lifecycle API.
     */
    protected abstract suspend fun doRegisterTable(table: Table)
}
