package io.github.heyhey123.skriptorm.database

import io.github.heyhey123.skriptorm.queries.Queries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.util.concurrent.atomic.AtomicReference

/**
 * One open transaction on one connection.
 *
 * A transaction owns a connection from the moment it begins until it commits or rolls back, and every
 * statement written inside it runs on that connection through [queries]. Who ends it is not always the
 * script: the timeout below, a disconnect, and plugin shutdown all end transactions on their own. That
 * is why the state here is the authority and a script that resumes afterwards is not: a continuation
 * that arrives too late finds a transaction which is no longer active and does nothing.
 *
 * The lease a [Database] hands out for the whole transaction is released once, in [finish], whichever
 * way the transaction ended. Without that, a script that errored inside a transaction would hold a
 * pooled connection until the server restarted.
 */
abstract class Transaction protected constructor(
    /** The database this transaction belongs to, and the one it holds a lease on. */
    val database: Database,

    /** How long the transaction may stay open before it is rolled back on its own. */
    val timeout: Duration
) {

    enum class State {
        ACTIVE,
        ROLLBACK_ONLY,
        COMMITTING,
        COMMITTED,
        ROLLING_BACK,
        ROLLED_BACK,
        ABORTED
    }

    private companion object {
        val TERMINAL = setOf(State.COMMITTED, State.ROLLED_BACK, State.ABORTED)
    }

    private val stateRef = AtomicReference(State.ACTIVE)

    private var watchdog: Job? = null

    /**
     * The first thing that went wrong, which is also what a script is told after a rollback. The first
     * one is kept on purpose: a later statement failing because the transaction is already rollback-only
     * says nothing about the cause.
     */
    @Volatile
    var failure: Throwable? = null
        private set

    val state: State
        get() = stateRef.get()

    /** Whether statements may still run. A rollback-only transaction is not active. */
    val isActive: Boolean
        get() = stateRef.get() == State.ACTIVE

    val isFinished: Boolean
        get() = stateRef.get() in TERMINAL

    /** Statements written inside the transaction run through this, on the pinned connection. */
    abstract val queries: Queries

    /**
     * Records that this transaction must not be committed, which is what a failed statement inside it
     * does. The transaction stays open so the script can keep running and be rolled back once.
     */
    fun markFailed(error: Throwable) {
        if (failure == null) failure = error
        stateRef.compareAndSet(State.ACTIVE, State.ROLLBACK_ONLY)
    }

    /**
     * Runs [block] on the pinned connection.
     *
     * @throws TransactionAbortedException when the transaction is no longer active, which a timeout or
     * a disconnect can have made true since the statement was written.
     */
    suspend fun <T> withQueries(block: suspend (Queries) -> T): T {
        if (!isActive) {
            throw TransactionAbortedException(
                "The database transaction is ${stateRef.get()} and cannot run this statement."
            )
        }
        return block(queries)
    }

    /**
     * Commits the transaction. Only the section that opened it does this, and only when the script
     * reached the end of its body.
     *
     * A failure here means the outcome is unknown rather than "nothing was written": the server may
     * have applied the transaction and lost the answer. It is never retried for that reason.
     */
    suspend fun commit() {
        if (!stateRef.compareAndSet(State.ACTIVE, State.COMMITTING)) {
            error("A transaction can only be committed while it is active, but it is ${stateRef.get()}.")
        }
        try {
            doCommit()
        } catch (error: Throwable) {
            if (failure == null) failure = error
            finish(State.ABORTED)
            throw error
        }
        finish(State.COMMITTED)
    }

    /** Rolls the transaction back and keeps the reason it failed, if it had one. */
    suspend fun rollback() {
        if (!claimForRollback()) return
        try {
            doRollback()
        } catch (error: Throwable) {
            if (failure == null) failure = error
        } finally {
            finish(State.ROLLED_BACK)
        }
    }

    /**
     * Ends the transaction from outside the script: the timeout, a disconnect, or plugin shutdown.
     *
     * Unlike [rollback] the state it leaves behind is [State.ABORTED], because nothing in the script
     * asked for it and a continuation that resumes later has to be able to tell the two apart.
     */
    internal suspend fun abort(reason: Throwable) {
        if (failure == null) failure = reason
        if (!claimForRollback()) return
        try {
            doRollback()
        } catch (error: Throwable) {
            if (failure == null) failure = error
        } finally {
            finish(State.ABORTED)
        }
    }

    /**
     * Arms the watchdog that rolls this transaction back when it stays open for longer than [timeout].
     *
     * This is not a convenience. A script can stop mid-body without any notification Skript offers us
     * (an exception in the body is caught by `TriggerItem.walk`), and a `wait` inside a transaction can
     * park it for minutes. Without a deadline the pinned connection, and the lease that keeps the
     * database from closing, would be held for the life of the server.
     */
    internal fun startWatchdog(scope: CoroutineScope) {
        watchdog = scope.launch {
            delay(timeout.toMillis())
            abort(
                TransactionAbortedException(
                    "The database transaction was open for longer than ${timeout.toSeconds()} seconds " +
                        "and was rolled back."
                )
            )
        }
    }

    /** Commits everything [doRollback] undoes, on the pinned connection. */
    protected abstract suspend fun doCommit()

    /** Undoes the statements run so far, on the pinned connection. */
    protected abstract suspend fun doRollback()

    /** Gives the pinned connection back, in a state the pool can hand to the next caller. */
    protected abstract suspend fun doRelease()

    /** Moves out of a state that may still roll back. False when something else got there first. */
    private fun claimForRollback(): Boolean {
        while (true) {
            val current = stateRef.get()
            if (current in TERMINAL || current == State.COMMITTING) return false
            if (stateRef.compareAndSet(current, State.ROLLING_BACK)) return true
        }
    }

    /** Ends the transaction exactly once: the watchdog stops, the connection goes back, the lease frees. */
    private suspend fun finish(state: State) {
        stateRef.set(state)
        watchdog?.cancel()
        watchdog = null
        withContext(NonCancellable) {
            try {
                doRelease()
            } catch (error: Throwable) {
                if (failure == null) failure = error
            } finally {
                database.transactionFinished(this@Transaction)
            }
        }
    }
}
