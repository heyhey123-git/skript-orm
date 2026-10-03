package io.github.heyhey123.skriptorm.database

import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.DataTypes
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.Duration
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A database connection and its operations, independent of the storage backend.
 */
abstract class Database {

    companion object {

        /**
         * How long a transaction may stay open before the watchdog rolls it back.
         *
         * This prevents a script that fails mid-transaction from holding a pooled connection
         * indefinitely. Scripts that need more time can set a timeout on the `database transaction`
         * section.
         */
        val DEFAULT_TRANSACTION_TIMEOUT: Duration = Duration.ofSeconds(30)

        /**
         * Coroutine scope for transaction watchdogs. Keeping it in the database layer protects
         * transactions opened through this API without introducing a Bukkit dependency.
         */
        private val watchdogScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /**
         * How long a disconnect waits for operations that already hold a lease before closing anyway.
         *
         * Waiting lets in-flight statements finish before the pool closes. The wait needs a limit:
         * drivers may ignore statement timeouts, and `commit` or `CREATE TABLE` may wait on locks.
         * MySQL's default `lock_wait_timeout`, for example, is one year. Without a separate limit,
         * one blocked operation could hold up `disconnect` and server shutdown.
         *
         * Implementations with different operation timeouts should override [Database.closeWaitTimeout].
         */
        val DEFAULT_CLOSE_WAIT_TIMEOUT: Duration = Duration.ofSeconds(30)

        /**
         * Receives lifecycle warnings. The plugin installs a logger on enable; the no-op default
         * keeps this layer independent of Bukkit and quiet in tests.
         */
        internal var warn: (String) -> Unit = {}

        /**
         * Mutex that protects the lifecycle state of the database: which connections are published,
         * the connection state of each, and the active operation count.
         */
        private val lifecycleMutex = Mutex()

        /**
         * Serializes connects, replacements, and disconnects. [publish] holds this lock through
         * connection setup, so each transition finishes or cleans up before the next begins.
         * [Database.State.CONNECTING] is therefore visible only outside the lifecycle. Implementations
         * must not call back into the lifecycle from [doConnect] while this lock is held.
         */
        private val transitionMutex = Mutex()

        /**
         * Named connections. Statements read this map on the server thread while lifecycle
         * transitions update it on other threads. Registration changes still use [lifecycleMutex].
         */
        private val connections: MutableMap<String, Database> = ConcurrentHashMap()

        /**
         * Recently closed connection names, oldest first. This lets errors distinguish an unknown
         * name from a connection that closed while a script was using it. The set is bounded to avoid
         * retaining every name on servers that create many short-lived connections.
         */
        private val closedNames: MutableSet<String> = Collections.synchronizedSet(
            object : LinkedHashSet<String>() {
                override fun add(element: String): Boolean {
                    val added = super.add(element)
                    if (added && size > CLOSED_NAME_LIMIT) {
                        val oldest = iterator()
                        if (oldest.hasNext()) {
                            oldest.next()
                            oldest.remove()
                        }
                    }
                    return added
                }
            }
        )

        private const val CLOSED_NAME_LIMIT = 64

        /** Remembers that [name] was connected and has since been closed. */
        internal fun noteClosed(name: String) {
            closedNames.add(name)
        }

        /** Whether [name] was connected and has since been closed. */
        internal fun wasClosed(name: String): Boolean = name in closedNames

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

        /** Registered connection names in a stable order for diagnostics. */
        val connectionNames: List<String>
            get() = connections.keys.sorted()

        /** The connection registered under [name], or null when no script has connected one yet. */
        fun connection(name: String): Database? = connections[name]

        /**
         * Makes a registered connection the default one.
         *
         * An unnamed previous default is disconnected because scripts can no longer reach it.
         * A named connection remains available through its name.
         *
         * @return false when no connected database carries that name.
         */
        suspend fun makeDefault(name: String): Boolean = transitionMutex.withLock {
            if (isShuttingDown) return@withLock false
            val candidate = connections[name] ?: return@withLock false
            if (!candidate.isConnected) return@withLock false

            val previous = lifecycleMutex.withLock {
                val current = defaultConnection
                defaultConnection = candidate
                current
            }
            if (previous != null && previous !== candidate && previous.connectionName == null) {
                previous.disconnectInternal()
            }
            true
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
         * Connects [database] and makes it the default connection, which is what a statement uses when
         * no scope or switch names another one.
         *
         * Whatever was the default before it is disconnected, unless a name keeps that connection
         * reachable. Concurrent requests are serialized by the transition lock: each one waits for the
         * previous transition, and then connects the requested instance.
         */
        suspend fun connectDefault(database: Database, settings: ConnectionSettings) {
            publish(null, database, settings)
        }

        /**
         * Connects [database] and registers it under [name], leaving every other connection alone.
         *
         * A name already in use is replaced, which is what reconnecting from a reloaded `on load`
         * looks like. The first connection to succeed becomes the default, so a script with one
         * connection never has to name it.
         */
        suspend fun connectNamed(name: String, database: Database, settings: ConnectionSettings) {
            publish(name, database, settings)
        }

        private suspend fun publish(name: String?, database: Database, settings: ConnectionSettings) {
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
                database.connectInternal(name, settings)
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

    /**
     * Transactions still open on this connection.
     *
     * They are tracked so that closing the connection can end them first. Disconnect waits for the
     * operations that hold a lease, and a transaction holds one for its whole life, so a transaction a
     * script left parked at a `wait` would otherwise block the close forever.
     */
    private val openTransactions = CopyOnWriteArrayList<Transaction>()

    /** Whether this implementation can pin one connection for a transaction. */
    open val supportsTransactions: Boolean
        get() = false

    /**
     * Opens a transaction, which pins one connection for its whole life and holds a lifecycle lease
     * until it ends.
     *
     * @param timeout how long the transaction may stay open; the watchdog rolls it back after that.
     * @throws IllegalStateException when this database is not connected, or its implementation cannot
     * open transactions at all.
     */
    suspend fun beginTransaction(timeout: Duration = DEFAULT_TRANSACTION_TIMEOUT): Transaction {
        check(supportsTransactions) { "This database implementation does not support transactions." }
        acquireOperation()
        try {
            val transaction = doBeginTransaction(timeout)
            openTransactions.add(transaction)
            transaction.startWatchdog(watchdogScope)
            return transaction
        } catch (error: Throwable) {
            withContext(NonCancellable) {
                releaseOperation()
            }
            throw error
        }
    }

    /**
     * Initializes implementation-specific resources for a transaction: a pinned connection with
     * automatic commits turned off.
     */
    protected open fun doBeginTransaction(timeout: Duration): Transaction =
        throw UnsupportedOperationException("This database implementation does not support transactions.")

    /** Called by a [Transaction] once it has handed its connection back. */
    internal suspend fun transactionFinished(transaction: Transaction) {
        openTransactions.remove(transaction)
        withContext(NonCancellable) {
            releaseOperation()
        }
    }

    /**
     * Rolls back open transactions before disconnect, including scripts suspended at a `wait`.
     */
    private suspend fun abortOpenTransactions() {
        for (transaction in openTransactions) {
            transaction.abort(
                TransactionAbortedException("The database connection was closed while this transaction was open.")
            )
        }
    }

    @Volatile
    var state: State = State.DISCONNECTED
        private set

    /**
     * The name this connection is registered under, or null when it was created without one.
     *
     * Statements can reach this connection only by name or as the default.
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

    /**
     * Maximum time disconnect waits for active operations before closing the connection.
     * Implementations may override this to account for their statement timeouts.
     */
    open val closeWaitTimeout: Duration
        get() = DEFAULT_CLOSE_WAIT_TIMEOUT

    private var disconnectCompletion: CompletableDeferred<Unit>? = null
    private var activeOperations: Int = 0
    private var operationsDrained: CompletableDeferred<Unit>? = null

    private suspend fun connectInternal(name: String?, settings: ConnectionSettings) {
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
            doConnect(settings)
            check(queries != null) { "Database implementation did not initialize queries during connection." }

            for (table in tables.values.toList()) {
                doRegisterTable(table)
            }

            lifecycleMutex.withLock {
                check(!isShuttingDown) { "Database lifecycle began shutting down while connecting." }
                state = State.CONNECTED
                connectionName = name
                if (name != null) {
                    connections[name] = this
                    // Clear any stale "closed connection" diagnostic for this name.
                    closedNames.remove(name)
                }
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
     * Removes this instance from the registry. The caller holds [lifecycleMutex].
     */
    private fun unpublish() {
        val name = connectionName
        // Failed connection attempts were never published and must not be recorded as closed.
        if (name != null && connections.remove(name, this)) noteClosed(name)
        if (defaultConnection === this@Database) defaultConnection = null
        connectionName = null
    }

    /** Whether this instance is still reachable, either as a named connection or as the default. */
    private fun isPublished(): Boolean {
        val name = connectionName
        return defaultConnection === this@Database || (name != null && connections[name] === this)
    }

    /** Initializes implementation-specific resources and [queries] from [settings]. */
    protected abstract fun doConnect(settings: ConnectionSettings)

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
     * Acquires an operation lease so disconnect waits for the operation to finish.
     *
     * @return queries for this connection
     * @throws IllegalStateException if the database is disconnected or shutting down
     */
    private suspend fun acquireOperation(): Queries = lifecycleMutex.withLock {
        check(!isShuttingDown) { "Database lifecycle is shutting down." }
        check(state == State.CONNECTED && isPublished()) { "Database is not connected." }
        val activeQueries = checkNotNull(queries) { "Database queries are not initialized." }
        activeOperations++
        activeQueries
    }

    /**
     * Releases an operation lease and wakes disconnect if no operations remain.
     *
     * @throws IllegalStateException if no operation holds a lease
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
        // Capture this call's waiters under the lifecycle lock, then wait outside it. A concurrent
        // disconnect may observe different state, and completing a waiter also needs this lock.

        /** Another disconnect is already running, so this caller waits for it instead of closing twice. */
        var existingDisconnect: CompletableDeferred<Unit>? = null

        /** Non-null when operations held a lease as this disconnect began; completed when the last ends. */
        var operationWaiter: CompletableDeferred<Unit>? = null

        /** Completed when this disconnect finishes, including after a failure. */
        var completion: CompletableDeferred<Unit>? = null

        lifecycleMutex.withLock {
            when (state) {
                State.DISCONNECTED -> return

                // The transition lock normally prevents this state; fail explicitly if that changes.
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
            // An open transaction holds a lease and may be suspended at a script's `wait`.
            // End transactions before waiting for other operations to drain.
            abortOpenTransactions()
            awaitOperations(operationWaiter)
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

    /**
     * Waits for the operations that already hold a lease, but not forever.
     *
     * A blocking JDBC call may never return, so shutdown cannot wait indefinitely. On timeout,
     * disconnect closes the connection and logs that in-flight operations may fail.
     */
    private suspend fun awaitOperations(waiter: CompletableDeferred<Unit>?) {
        if (waiter == null) return
        val outstanding = lifecycleMutex.withLock { activeOperations }
        try {
            withTimeout(closeWaitTimeout.toMillis()) {
                waiter.await()
            }
        } catch (_: TimeoutCancellationException) {
            // Discard this waiter's latch; the connection will be marked disconnected either way.
            lifecycleMutex.withLock {
                operationsDrained = null
            }
            warn(
                "Closing the database connection while $outstanding database operation(s) are still " +
                    "running. They will fail, and the connection they hold is being closed underneath them."
            )
        }
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
