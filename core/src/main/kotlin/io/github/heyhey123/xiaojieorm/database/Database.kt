package io.github.heyhey123.xiaojieorm.database

import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.DataTypes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a generic database connection and operations.
 */
abstract class Database {
    companion object {
        /**
         * Mutex that protects the lifecycle state of the database, including the current and pending connections,
         * the connection state, and the active operation count.
         */
        private val lifecycleMutex = Mutex()

        /**
         * Mutex that serializes concurrent requests to replace or disconnect the database connection.
         * Each request waits for any pending connection attempt to complete, disconnects the active database, and then connects the requested instance.
         */
        private val transitionMutex = Mutex()

        /** The currently active, fully initialized database connection. */
        @Volatile
        var current: Database? = null
            private set

        @Volatile
        var isShuttingDown: Boolean = false
            private set

        /**
         * The database that is currently in the process of connecting.
         * This is not yet fully initialized and should not be used for operations.
         */
        private var pending: Database? = null

        /** Reopens the global database lifecycle when the plugin is enabled. */
        fun beginLifecycle() {
            check(current == null && pending == null) {
                "Cannot begin a database lifecycle while resources from the previous lifecycle still exist."
            }
            isShuttingDown = false
        }

        /**
         * Replaces the current database connection. Concurrent replacement requests are serialized;
         * each request waits for an earlier connection attempt, disconnects the active database, and
         * then connects the requested instance.
         */
        suspend fun replaceWith(database: Database, url: String, user: String, password: String) {
            transitionMutex.withLock {
                check(!isShuttingDown) { "Database lifecycle is shutting down." }

                val pendingCompletion = lifecycleMutex.withLock { pending?.connectCompletion }
                pendingCompletion?.await()

                val active = lifecycleMutex.withLock { current }
                active?.disconnectInternal()

                check(!isShuttingDown) { "Database lifecycle is shutting down." }
                database.connectInternal(url, user, password)
            }
        }

        /**
         * Prevents new connections and operations, waits for a connection attempt already in progress,
         * and then disconnects the active database.
         */
        suspend fun shutdown() {
            lifecycleMutex.withLock {
                isShuttingDown = true
            }
            transitionMutex.withLock {
                val pendingCompletion = lifecycleMutex.withLock { pending?.connectCompletion }
                pendingCompletion?.await()
                val active = lifecycleMutex.withLock { current }
                if (active != null) {
                    withContext(NonCancellable) {
                        active.disconnectInternal()
                    }
                }
            }
        }
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

    private var connectCompletion: CompletableDeferred<Unit>? = null
    private var disconnectCompletion: CompletableDeferred<Unit>? = null
    private var activeOperations: Int = 0
    private var operationsDrained: CompletableDeferred<Unit>? = null

    private suspend fun connectInternal(url: String, user: String, password: String) {
        val completion = CompletableDeferred<Unit>()
        lifecycleMutex.withLock {
            check(!isShuttingDown) { "Database lifecycle is shutting down." }
            check(state == State.DISCONNECTED) { "Database is not disconnected: $state." }
            check(current == null && pending == null) {
                "Another database is already connected or connecting. Disconnect it before connecting a new database."
            }

            state = State.CONNECTING
            connectCompletion = completion
            pending = this
        }

        try {
            doConnect(url, user, password)
            check(queries != null) { "Database implementation did not initialize queries during connection." }

            for (table in tables.values.toList()) {
                doRegisterTable(table)
            }

            lifecycleMutex.withLock {
                check(!isShuttingDown) { "Database lifecycle began shutting down while connecting." }
                check(pending === this) { "This database is no longer the pending connection." }
                state = State.CONNECTED
                current = this
                pending = null
            }
        } catch (error: Throwable) {
            withContext(NonCancellable) {
                try {
                    doDisconnect()
                } catch (cleanupError: Throwable) {
                    error.addSuppressed(cleanupError)
                }
                lifecycleMutex.withLock {
                    queries = null
                    state = State.DISCONNECTED
                    if (pending === this@Database) pending = null
                    if (current === this@Database) current = null
                }
            }
            throw error
        } finally {
            completion.complete(Unit)
            withContext(NonCancellable) {
                lifecycleMutex.withLock {
                    if (connectCompletion === completion) connectCompletion = null
                }
            }
        }
    }

    /** Initializes implementation-specific resources and [queries]. */
    protected abstract fun doConnect(url: String, user: String, password: String)

    /**
     * Runs an operation while holding a lifecycle lease. Disconnect prevents new leases and waits
     * for every already-acquired lease before closing implementation resources.
     */
    suspend fun <T> withQueries(operation: suspend (Queries) -> T): T {
        val activeQueries = acquireOperation()
        try {
            return operation(activeQueries)
        } finally {
            withContext(NonCancellable) {
                releaseOperation()
            }
        }
    }

    /**
     * Acquires a lease for a database operation,
     * preventing disconnect until the operation is complete.
     * It is used to ensure that the database remains connected while operations are in progress.
     * Otherwise, the resources could be released while an operation is still using them, leading to undefined behavior.
     * @throws IllegalStateException if the database is not connected or is shutting down
     * @return the queries object for the current database connection
     */
    private suspend fun acquireOperation(): Queries = lifecycleMutex.withLock {
        check(!isShuttingDown) { "Database lifecycle is shutting down." }
        check(state == State.CONNECTED && current === this) { "Database is not connected." }
        val activeQueries = checkNotNull(queries) { "Database queries are not initialized." }
        activeOperations++
        activeQueries
    }

    /**
     * Releases a lease for a database operation,
     * allowing disconnect to proceed if no other operations are active.
     * @throws IllegalStateException if no operation is currently active
     */
    private suspend fun releaseOperation() {
        var drained: CompletableDeferred<Unit>? = null
        lifecycleMutex.withLock {
            check(activeOperations > 0) { "Database operation lease underflow." }
            activeOperations--
            if (activeOperations == 0) {
                drained = operationsDrained
                operationsDrained = null
            }
        }
        drained?.complete(Unit)
    }

    /**
     * Disconnects this database after all operations that already acquired a lease have completed.
     * Once disconnect begins, no new operation can acquire a lease.
     */
    suspend fun disconnect() = transitionMutex.withLock {
        disconnectInternal()
    }

    private suspend fun disconnectInternal() {
        var existingDisconnect: CompletableDeferred<Unit>? = null
        var operationWaiter: CompletableDeferred<Unit>? = null
        var completion: CompletableDeferred<Unit>? = null

        lifecycleMutex.withLock {
            when (state) {
                State.DISCONNECTED -> return
                State.CONNECTING -> error("Cannot disconnect a database while it is still connecting.")
                State.DISCONNECTING -> existingDisconnect = disconnectCompletion
                State.CONNECTED -> {
                    state = State.DISCONNECTING
                    completion = CompletableDeferred()
                    disconnectCompletion = completion
                    if (activeOperations > 0) {
                        operationWaiter = CompletableDeferred()
                        operationsDrained = operationWaiter
                    }
                }
            }
        }

        existingDisconnect?.let {
            it.await()
            return
        }

        var failure: Throwable? = null
        withContext(NonCancellable) {
            operationWaiter?.await()
            try {
                doDisconnect()
            } catch (error: Throwable) {
                failure = error
            } finally {
                lifecycleMutex.withLock {
                    queries = null
                    state = State.DISCONNECTED
                    if (pending === this@Database) pending = null
                    if (current === this@Database) current = null
                    disconnectCompletion = null
                }
                completion?.complete(Unit)
            }
        }
        failure?.let { throw it }
    }

    /** Releases implementation-specific resources. */
    protected abstract fun doDisconnect()

    /**
     * Registers a table while holding an operation lease and publishes it only after registration succeeds.
     */
    suspend fun registerTable(table: Table) {
        acquireOperation()
        try {
            doRegisterTable(table)
            tables[table.name] = table
        } finally {
            withContext(NonCancellable) {
                releaseOperation()
            }
        }
    }

    /** Structured registration hook for implementations. */
    protected abstract suspend fun doRegisterTable(table: Table)
}
