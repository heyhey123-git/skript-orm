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
 * Every statement runs through [queries] on the same pinned connection until commit or rollback.
 * A timeout, disconnect, or plugin shutdown may end the transaction before the script resumes;
 * subsequent statements then see an inactive transaction.
 *
 * [finish] releases the [Database] lease exactly once, regardless of how the transaction ends.
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
     * The first failure reported to the script after rollback. Later errors may result from the
     * transaction already being rollback-only, so they must not replace the original cause.
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

    /**
     * Whether [doRollback] failed, leaving the outcome uncertain. Combined with
     * [hasStatementInFlight] by [mustDiscardConnection].
     */
    private var undoIsUncertain = false

    /**
     * Whether the pinned connection may still be running a statement from this transaction.
     *
     * A statement that finishes after rollback may be committed when automatic commits are restored.
     * Implementations that can detect in-flight statements override this method.
     */
    protected open fun hasStatementInFlight(): Boolean = false

    /**
     * Whether the pinned connection must be thrown away rather than returned to the pool.
     *
     * True when the undo cannot be trusted — the rollback failed, or a statement was still running while
     * it was issued — because such a connection may carry work this transaction meant to undo, and a
     * connection whose automatic commits are restored commits that work on the way back. A connection in
     * this state is closed for good instead, and the pool hands the next statement a fresh one.
     */
    protected val mustDiscardConnection: Boolean
        get() = undoIsUncertain || hasStatementInFlight()

    /** Statements written inside the transaction run through this, on the pinned connection. */
    abstract val queries: Queries

    /**
     * Records that this transaction must not be committed, which is what a failed statement inside it
     * does. The transaction stays open so the script can keep running and be rolled back once.
     */
    fun markFailed(error: Throwable) {
        record(error)
        stateRef.compareAndSet(State.ACTIVE, State.ROLLBACK_ONLY)
    }

    /**
     * Keeps [error] as the first failure, or suppresses it under an earlier failure. This preserves
     * the cause reported to the script while retaining later rollback errors for diagnostics.
     */
    private fun record(error: Throwable) {
        val first = failure
        if (first == null) {
            failure = error
        } else if (first !== error) {
            first.addSuppressed(error)
        }
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
     * If commit fails, the server may still have applied the transaction. The outcome is unknown,
     * so commit is not retried.
     */
    suspend fun commit() {
        if (!stateRef.compareAndSet(State.ACTIVE, State.COMMITTING)) {
            error("A transaction can only be committed while it is active, but it is ${stateRef.get()}.")
        }
        try {
            doCommit()
        } catch (error: Throwable) {
            record(error)
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
            undoIsUncertain = true
            record(error)
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
        record(reason)
        if (!claimForRollback()) return
        // Check before rollback: an in-flight statement may finish after it, leaving the connection unsafe to reuse.
        if (hasStatementInFlight()) undoIsUncertain = true
        try {
            doRollback()
        } catch (error: Throwable) {
            undoIsUncertain = true
            record(error)
        } finally {
            finish(State.ABORTED)
        }
    }

    /**
     * Arms the watchdog that rolls this transaction back when it stays open for longer than [timeout].
     *
     * Skript may catch an exception inside the body without notifying this transaction, and a `wait`
     * may suspend it for minutes. The deadline prevents either case from holding the connection and
     * its lifecycle lease indefinitely.
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

    /** Stops the watchdog, releases the connection, and returns the lifecycle lease. */
    private suspend fun finish(state: State) {
        stateRef.set(state)
        watchdog?.cancel()
        watchdog = null
        withContext(NonCancellable) {
            try {
                doRelease()
            } catch (error: Throwable) {
                record(error)
            } finally {
                database.transactionFinished(this@Transaction)
            }
        }
    }
}
