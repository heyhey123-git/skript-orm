package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.TriggerItem
import ch.njol.skript.lang.Variable
import io.github.heyhey123.skriptorm.SkriptOrm
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.database.Transaction
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.utils.BenchmarkTimings
import io.github.heyhey123.skriptorm.utils.SyncDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.skriptlang.skript.log.runtime.RuntimeErrorProducer

/**
 * Shared connection checks, error reporting, and asynchronous execution for database elements.
 * Section and effect forms use the same trigger and local-variable handling.
 */
internal object DatabaseWork {

    /**
     * The resolved database, table, and optional transaction for a statement. The transaction is
     * captured on the server thread before the query runs asynchronously.
     */
    class Target(
        val database: Database,
        val table: Table,
        val transaction: Transaction?
    ) {

        /**
         * Runs [block] on the transaction's pinned connection, or a pooled connection.
         */
        suspend fun <T> withQueries(block: suspend (Queries) -> T): T =
            DatabaseWork.withQueries(database, transaction, block)
    }

    /**
     * The resolved database and optional transaction for a raw statement, which names no table.
     */
    class ConnectionTarget(
        val database: Database,
        val transaction: Transaction?
    ) {

        /** Runs [block] on the transaction's pinned connection, or one borrowed from the pool. */
        suspend fun <T> withQueries(block: suspend (Queries) -> T): T =
            DatabaseWork.withQueries(database, transaction, block)
    }

    /**
     * Resolves a raw statement's connection. On failure, reports the error and returns null so the
     * caller can continue to the next trigger item.
     */
    fun resolveConnection(event: Event, producer: RuntimeErrorProducer): ConnectionTarget? {
        if (skipInactiveTransaction(event, producer)) return null

        val database = ConnectionScope.resolve(event) ?: run {
            report(event, producer, ConnectionScope.noConnectionMessage())
            return null
        }

        if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) {
            report(event, producer, "Database lifecycle is shutting down.")
            return null
        }

        return ConnectionTarget(database, ConnectionScope.transaction(event))
    }

    /**
     * Resolves the table named by [tableNameExpr]. On failure, reports through [producer] so Skript
     * identifies the source line, then returns null for the caller to continue the trigger.
     */
    fun resolveTable(
        event: Event,
        producer: RuntimeErrorProducer,
        tableNameExpr: Expression<String>
    ): Target? {
        if (skipInactiveTransaction(event, producer)) return null

        val database = ConnectionScope.resolve(event) ?: run {
            report(event, producer, ConnectionScope.noConnectionMessage())
            return null
        }

        val tableName = tableNameExpr.getSingle(event) ?: run {
            report(event, producer, "Table name is null.")
            return null
        }

        val table = database.tables[tableName] ?: run {
            report(event, producer, "Table '$tableName' not found.")
            return null
        }

        if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) {
            report(event, producer, "Database lifecycle is shutting down.")
            return null
        }

        return Target(database, table, ConnectionScope.transaction(event))
    }

    /**
     * Skips statements in an inactive transaction. A rollback-only transaction keeps its original
     * `last database error`; an externally aborted transaction reports its failure here.
     */
    fun skipInactiveTransaction(event: Event, producer: RuntimeErrorProducer): Boolean {
        val transaction = ConnectionScope.transaction(event) ?: return false
        if (transaction.isActive) return false
        if (transaction.state != Transaction.State.ROLLBACK_ONLY) {
            report(
                event,
                producer,
                transaction.failure?.message
                    ?: "The database transaction is ${transaction.state} and cannot run this statement."
            )
        }
        return true
    }

    /**
     * Stores [message] as the last database error and reports it through [producer]. If a
     * transaction is active, marks it rollback-only even when the statement never reached the database.
     */
    fun report(event: Event?, producer: RuntimeErrorProducer, message: String) {
        event?.let {
            ConnectionScope.transaction(it)?.markFailed(IllegalStateException(message))
            SkriptDatabaseErrors.set(it, message)
        }
        producer.error(message)
    }

    /**
     * Stores a database failure and marks the active transaction rollback-only. Subsequent
     * statements in that transaction are skipped, preserving the original error for the script.
     */
    fun recordFailure(event: Event?, error: Throwable) {
        event?.let {
            ConnectionScope.transaction(it)?.markFailed(error)
            SkriptDatabaseErrors.set(it, error)
        }
    }

    /**
     * Clears a refused read's result variable before reporting [message], so an earlier result cannot
     * be mistaken for the result of this statement.
     */
    fun refuseRead(
        event: Event,
        producer: RuntimeErrorProducer,
        message: String,
        resultVar: Variable<*>
    ) {
        VariableModifier.clear(resultVar, event)
        report(event, producer, message)
    }

    /**
     * Clears the last database error before a standalone statement. Transactions retain the error
     * that made them rollback-only.
     */
    fun clearErrorForStatement(event: Event) {
        if (ConnectionScope.transaction(event) == null) {
            SkriptDatabaseErrors.clear(event)
        }
    }

    /**
     * Runs [block] on the captured transaction's connection, or borrows one from the database.
     */
    suspend fun <T> withQueries(
        database: Database,
        transaction: Transaction?,
        block: suspend (Queries) -> T
    ): T = transaction?.withQueries(block) ?: database.withQueries(block)

    /**
     * Parks the trigger, runs [query] off the server thread, then calls [deliver] and resumes
     * [continuation] on the server thread. Returns null for the caller's `walk` method.
     *
     * If supplied, [prepare] runs one slice per server tick. It returns false while more work remains;
     * the next slice runs on the following tick. Event local variables are restored for each slice and
     * removed between slices. [query] starts only after preparation returns true.
     *
     * Set [clearErrorOnSuccess] to false when successful cleanup must preserve an earlier failure,
     * such as rolling back a failed transaction.
     */
    fun <T : Any> run(
        event: Event,
        continuation: TriggerItem?,
        query: suspend () -> T,
        deliver: (T) -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
        clearErrorOnSuccess: Boolean = true,
        prepare: (() -> Boolean)? = null,
        resultVariable: Variable<*>? = null
    ): Nothing? {
        val timing = BenchmarkTimings.begin()
        val localWriter = resultVariable?.let {
            BenchmarkTimings.main(timing, BenchmarkTimings.Stage.RESULT) { LocalResultWriter.capture(it, event) }
        }
        var localVariables = SkriptLocalVariables.remove(event)
        Delay.addDelayedEvent(event)

        SkriptOrm.ioScope.launch(BenchmarkTimings.context(timing)) {
            var result: T? = null
            var failure: Throwable? = null
            var localResultWritten = false
            try {
                if (prepare != null) {
                    while (true) {
                        val ready = withContext(SyncDispatcher) {
                            if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) {
                                throw CancellationException("Database lifecycle is shutting down.")
                            }
                            try {
                                if (localVariables != null) {
                                    SkriptLocalVariables.restore(event, localVariables)
                                }
                                val transaction = ConnectionScope.transaction(event)
                                check(transaction == null || transaction.isActive) {
                                    transaction?.failure?.message ?: "The database transaction is no longer active."
                                }
                                BenchmarkTimings.main(timing, BenchmarkTimings.Stage.PREPARE) { prepare() }
                            } finally {
                                SkriptLocalVariables.clear(event)
                            }
                        }
                        if (ready) break
                        awaitNextTick()
                    }
                }
                val queryStarted = if (timing != null) System.nanoTime() else 0L
                try {
                    result = query()
                } finally {
                    if (timing != null) timing.executionNs = System.nanoTime() - queryStarted
                }
                if (result is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val rows = result as Map<String, Any?>
                    if (localWriter != null) {
                        localVariables = localWriter.write(localVariables, rows)
                        localResultWritten = true
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        val resolved = QueryResultValues.resolve(rows) as T
                        result = resolved
                        if (resultVariable != null) {
                            BenchmarkTimings.async(BenchmarkTimings.Stage.RESULT) {
                                FastVariableStore.publish(resolved as Map<String, Any?>)
                            }
                        }
                    }
                }
            } catch (_: CancellationException) {
                return@launch
            } catch (error: Throwable) {
                failure = error
            }

            withContext(NonCancellable + SyncDispatcher) {
                if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) return@withContext
                try {
                    if (localVariables != null) {
                        SkriptLocalVariables.restore(event, localVariables)
                    }

                    val error = failure
                    if (error != null) {
                        recordFailure(event, error)
                        onFailure(error)
                    } else {
                        if (clearErrorOnSuccess) SkriptDatabaseErrors.clear(event)
                        if (!localResultWritten) {
                            try {
                                BenchmarkTimings.main(timing, BenchmarkTimings.Stage.RESULT) { deliver(checkNotNull(result)) }
                            } catch (error: Throwable) {
                                recordFailure(event, error)
                                onFailure(error)
                            }
                        }
                    }

                    timing?.finish()
                    TriggerItem.walk(continuation, event)
                } finally {
                    SkriptLocalVariables.clear(event)
                }
            }
        }

        // Park the trigger until the coroutine resumes it.
        return null
    }

    private suspend fun awaitNextTick() = suspendCancellableCoroutine<Unit> { continuation ->
        val task = Bukkit.getScheduler().runTaskLater(
            SkriptOrm.instance,
            Runnable { continuation.resumeWith(Result.success(Unit)) },
            1L
        )
        continuation.invokeOnCancellation { task.cancel() }
    }
}
