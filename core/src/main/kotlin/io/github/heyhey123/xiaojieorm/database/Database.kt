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
         * Mutex that protects the lifecycle state of the database: which connections are published,
         * the connection state of each, and the active operation count.
         */
        private val lifecycleMutex = Mutex()

        /**
         * Mutex that serializes every transition: connecting, replacing, and disconnecting.
         *
         * [publish] performs the whole connect while holding this lock, so a connect can never
         * overlap another transition. Nothing has to track a connection attempt that is still in
         * flight, because by the time a transition acquires this lock the previous one has either
         * published its database or cleaned up after failing. The same invariant is why
         * [Database.State.CONNECTING] is only visible to observers outside the lifecycle, and why an
         * implementation must never call back into the lifecycle from [doConnect].
         */
        private val transitionMutex = Mutex()

        /**
         * Connections a script registered under a name.
         *
         * The map is concurrent because statements resolve a connection on the server thread while
         * the lifecycle updates registrations from whatever thread performs the transition. The rules
         * about who may be added, replaced and removed still run under [lifecycleMutex].
         */
        private val connections: MutableMap<String, Database> = ConcurrentHashMap()

        /**
         * The connection a statement uses when neither a scope nor an event names one.
         *
         * This is a field rather than an entry of [connections] for two reasons: a connection created
         * without a name is not registered under one at all, and a named connection may also be the
         * default, so the two roles are not the same thing.
         */
        @Volatile
        private var defaultConnection: Database? = null

        /** The default connection, or null when nothing is connected. */
        val current: Database?
            get() = defaultConnection

        /** The registered names, sorted so that a message reads the same way twice. */
        val connectionNames: List<String>
            get() = connections.keys.sorted()

        /** The connection registered under [name], or null when no script has connected one yet. */
        fun connection(name: String): Database? = connections[name]

        /**
         * Makes a registered connection the default one.
         *
         * @return false when no connected database carries that name.
         */
        fun makeDefault(name: String): Boolean {
            if (isShuttingDown) return false
            val candidate = connections[name] ?: return false
            if (!candidate.isConnected) return false
            defaultConnection = candidate
            return true
        }

        @Volatile
        var isShuttingDown: Boolean = false
            private set

        /** Reopens the global database lifecycle when the plugin is enabled. */
        fun beginLifecycle() {
            check(defaultConnection == null && connections.isEmpty()) {
                "Cannot begin a database lifecycle while resources from the previous lifecycle still exist."
            }
            isShuttingDown = false
        }

        /**
         * Replaces the default database connection. Concurrent replacement requests are serialized by
         * the transition lock: each one waits for the previous transition, disconnects the active
         * database, and then connects the requested instance.
         */
        suspend fun replaceWith(database: Database, url: String, user: String, password: String) {
            publish(null, database, url, user, password)
        }

        /**
         * Connects [database] and registers it under [name], leaving every other connection alone.
         *
         * A name already in use is replaced, which is what reconnecting from a reloaded `on load`
         * looks like. The first connection to succeed becomes the default, so a script with one
         * connection never has to name it.
         */
        suspend fun createConnection(
            name: String,
            database: Database,
            url: String,
            user: String,
            password: String
        ) {
            publish(name, database, url, user, password)
        }

        private suspend fun publish(
            name: String?,
            database: Database,
            url: String,
            user: String,
            password: String
        ) {
            transitionMutex.withLock {
                check(!isShuttingDown) { "Database lifecycle is shutting down." }

                // An unnamed connection replaces the default, which is what `create a connection`
                // without a name has always done. A named one replaces only its own name.
                //
                // The previous default is disconnected only when no name keeps it reachable. A named
                // connection that happened to be the default keeps running and merely stops being the
                // default, so mixing the two forms cannot silently close a connection the script
                // registered under a name. The role is vacated either way, which is what a failed
                // connect is allowed to leave behind.
                val replaced = lifecycleMutex.withLock {
                    if (name == null) {
                        val previous = defaultConnection
                        defaultConnection = null
                        previous?.takeIf { it.connectionName == null }
                    } else {
                        connections[name]
                    }
                }
                if (replaced != null && replaced !== database) replaced.disconnectInternal()

                check(!isShuttingDown) { "Database lifecycle is shutting down." }
                database.connectInternal(url, user, password, name)
            }
        }

        /**
         * Prevents new connections and operations and then disconnects every connection. Taking the
         * transition lock also waits for a connection attempt that is already in progress.
         */
        suspend fun shutdown() {
            lifecycleMutex.withLock {
                isShuttingDown = true
            }
            withContext(NonCancellable) {
                disconnectAllInternal()
            }
        }

        /** Disconnects every connection without closing the lifecycle. */
        suspend fun disconnectAll() {
            disconnectAllInternal()
        }

        /**
         * Disconnects every published connection, reporting the first failure once the rest have
         * been closed. The snapshot is taken before the first disconnect, because each one removes
         * its own registration.
         */
        private suspend fun disconnectAllInternal() {
            transitionMutex.withLock {
                val active = lifecycleMutex.withLock {
                    (listOfNotNull(defaultConnection) + connections.values).distinct()
                }

                var failure: Throwable? = null
                for (database in active) {
                    try {
                        database.disconnectInternal()
                    } catch (error: Throwable) {
                        if (failure == null) failure = error else failure.addSuppressed(error)
                    }
                }
                failure?.let { throw it }
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

    /**
     * The name this connection is registered under, or null when it was created without one.
     *
     * A script can only reach a connection through its name or by it being the default, so this is
     * also what tells a statement whether the instance in front of it is still reachable.
     */
    @Volatile
    var connectionName: String? = null
        private set

    val isConnected: Boolean
        get() = state == State.CONNECTED

    /** Queries specific to the database implementation. */
    @Volatile
    var queries: Queries? = null
        protected set

    /** Data types supported by this database. */
    abstract val dataTypes: DataTypes

    private var disconnectCompletion: CompletableDeferred<Unit>? = null
    private var activeOperations: Int = 0
    private var operationsDrained: CompletableDeferred<Unit>? = null

    private suspend fun connectInternal(url: String, user: String, password: String, name: String?) {
        lifecycleMutex.withLock {
            check(!isShuttingDown) { "Database lifecycle is shutting down." }
            check(state == State.DISCONNECTED) { "Database is not disconnected: $state." }
            if (name == null) {
                check(defaultConnection == null) {
                    "Another database is already connected. Disconnect it before connecting a new database."
                }
            } else {
                val existing = connections[name]
                check(existing == null || existing === this) {
                    "A connection named '$name' is already registered."
                }
            }

            state = State.CONNECTING
        }

        try {
            doConnect(url, user, password)
            check(queries != null) { "Database implementation did not initialize queries during connection." }

            for (table in tables.values.toList()) {
                doRegisterTable(table)
            }

            lifecycleMutex.withLock {
                check(!isShuttingDown) { "Database lifecycle began shutting down while connecting." }
                state = State.CONNECTED
                connectionName = name
                if (name != null) connections[name] = this
                if (name == null || defaultConnection == null) defaultConnection = this
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
                    unpublish()
                }
            }
            throw error
        }
    }

    /**
     * Takes this instance out of the registry. Called while holding [lifecycleMutex], where the
     * lifecycle's own bookkeeping about what is published lives.
     */
    private fun unpublish() {
        val name = connectionName
        if (name != null) connections.remove(name, this)
        if (defaultConnection === this@Database) defaultConnection = null
        connectionName = null
    }

    /** Whether this instance is still reachable, either as a named connection or as the default. */
    private fun isPublished(): Boolean {
        val name = connectionName
        return defaultConnection === this@Database || (name != null && connections[name] === this)
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
        check(state == State.CONNECTED && isPublished()) { "Database is not connected." }
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

                // Unreachable as long as connectInternal only runs inside publish, which holds
                // the transition lock every caller of this method also takes. Kept as a guard
                // because falling through would close a database that is still initializing.
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
                    unpublish()
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
