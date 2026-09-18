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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Represents a generic database connection and operations.
 */
abstract class Database {

    companion object {

        /**
         * How long a transaction may stay open before the watchdog rolls it back.
         *
         * Long enough that no honest transaction meets it, short enough that a script which errored
         * inside one does not pin a pooled connection for the life of the server. A script that needs
         * longer says so on its own `database transaction` section.
         */
        val DEFAULT_TRANSACTION_TIMEOUT: Duration = Duration.ofSeconds(30)

        /**
         * Scope the transaction watchdogs run on.
         *
         * It belongs to the database layer rather than to the plugin, so that a transaction opened
         * through this API is protected even when no plugin is driving it, and so that the classes here
         * stay free of Bukkit.
         */
        private val watchdogScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /**
         * How long a disconnect waits for operations that already hold a lease before closing anyway.
         *
         * The wait is what keeps the pool from being closed under a statement that is still running, but
         * it is a wait on someone else's code: a statement whose timeout the driver ignores, a `commit`
         * waiting on a lock, or a `CREATE TABLE` waiting on a metadata lock, which MySQL bounds by
         * `lock_wait_timeout` and whose default is a year. Without a bound of our own, one such operation
         * hangs `disconnect`, and with it server shutdown, because the plugin's own disable runs
         * `shutdown` while the main thread waits for it.
         *
         * An implementation that knows its own limits overrides [Database.closeWaitTimeout] rather than
         * leaving this value to cover operations it knows can run longer.
         */
        val DEFAULT_CLOSE_WAIT_TIMEOUT: Duration = Duration.ofSeconds(30)

        /**
         * Where the lifecycle says what it had to do to finish, installed by the plugin when it enables.
         *
         * The classes here stay free of Bukkit, so they cannot reach a plugin logger; a no-op default
         * keeps them usable, and in tests, silent.
         */
        internal var warn: (String) -> Unit = {}

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
         * The connection that had the role is disconnected when no name keeps it reachable, which is the
         * rule [publish] applies when an unnamed connection replaces the default: an unnamed connection
         * that is no longer the default can be resolved by no statement at all, so leaving it running
         * would leave a pool, and the server connections in it, that nothing can close. A named one keeps
         * running and merely stops being the default, because a scope or a `use connection` may still be
         * using it.
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
     * Rolls back every transaction still open here, so that whatever is closing the connection does not
     * have to wait for a script that may never resume.
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

    /**
     * How long this database's disconnect waits for operations that hold a lease before closing anyway.
     *
     * Longer than the slowest operation it can legitimately be running, so that work which is about to
     * finish is not killed by a shutdown. An implementation with its own statement timeout says so here.
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
        // Three answers this method gets under the lifecycle lock and acts on outside it. None of them can
        // be a field: a second caller arriving mid-disconnect reads the field, while these carry what this
        // call found. None of the waiting may happen while the lock is held either, because the code that
        // completes a deferred takes that same lock.

        /** Another disconnect is already running, so this caller waits for it instead of closing twice. */
        var existingDisconnect: CompletableDeferred<Unit>? = null

        /** Non-null when operations held a lease as this disconnect began; completed when the last ends. */
        var operationWaiter: CompletableDeferred<Unit>? = null

        /** Completed when this disconnect has finished, whichever way it went, to release the waiters. */
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
            // Transactions hold a lease for their whole life, and one of them may be parked at a
            // script's `wait`, so waiting for them to finish on their own would wait forever. They are
            // ended first, which is what lets the rest of this method mean "no work is left".
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
     * A lease is released by the operation's own code finishing, and that code is a blocking JDBC call
     * nothing here can interrupt. Waiting without a bound for it is waiting for something that may never
     * come, and the cost of that is a server that cannot be stopped; closing the pool under a statement
     * that never finishes costs one failed operation, and says so in the log.
     */
    private suspend fun awaitOperations(waiter: CompletableDeferred<Unit>?) {
        if (waiter == null) return
        val outstanding = lifecycleMutex.withLock { activeOperations }
        try {
            withTimeout(closeWaitTimeout.toMillis()) {
                waiter.await()
            }
        } catch (_: TimeoutCancellationException) {
            // The next disconnect starts from a clean latch: this one belongs to the operations that
            // are still running, and the state is going to DISCONNECTED either way.
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
