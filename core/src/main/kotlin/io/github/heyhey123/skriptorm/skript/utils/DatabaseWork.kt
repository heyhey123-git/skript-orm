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
import io.github.heyhey123.skriptorm.utils.SyncDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event
import org.skriptlang.skript.log.runtime.RuntimeErrorProducer

/**
 * What every asynchronous database element shares: the checks made before a statement is sent, and the
 * hand-off that runs a query off the server thread and brings the trigger back to it afterwards.
 *
 * Both are lifted from what the sections already do, down to the order of the local-variable handling,
 * because a statement written without a colon has to behave exactly like the section form it shadows.
 * Only the query and the result differ between elements.
 */
internal object DatabaseWork {

    /**
     * The connection and the table a resolved statement works against, and the transaction it belongs to
     * when there is one.
     *
     * The transaction travels with the target rather than being looked up again later because the scope
     * it lives in belongs to the server thread: by the time a query runs, the frames may have moved on.
     */
    class Target(
        val database: Database,
        val table: Table,
        val transaction: Transaction?
    ) {

        /**
         * Runs [block] on the connection the transaction pinned, or on one borrowed from the pool when
         * the statement runs on its own.
         */
        suspend fun <T> withQueries(block: suspend (Queries) -> T): T =
            DatabaseWork.withQueries(database, transaction, block)
    }

    /**
     * Resolves the table [tableNameExpr] names, reporting a failure the way Skript itself reports one.
     *
     * [producer] is the effect or section the statement was written as. Reporting through it is what puts
     * the script, the syntax and the line into the console, and what tells the players watching for
     * runtime errors which line is failing.
     *
     * Returns null when the caller should let the trigger carry on with its next item instead; a message
     * for `last database error` has already been stored at that point.
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
     * Whether a statement has to be skipped because the transaction it belongs to can no longer take it.
     *
     * A transaction that failed a statement leaves `last database error` as it is: the cause is already
     * in there, and replacing it with "the transaction is rollback-only" would lose the only thing worth
     * reading. One that was aborted from outside (its timeout, a disconnect) never had the chance to say
     * anything, so it says it now.
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
     * Reports [message] the way a section does: as the last database error, and through [producer].
     *
     * The console half goes through the same channel Skript's own effects use, so the line is named, the
     * syntax is named and the script line is printed with it, and a server operator watching for runtime
     * errors hears about it. Skript applies its own frame limits to those lines: a script that fails the
     * same line over and over is reported the first time and summarised afterwards.
     *
     * It also marks the transaction in effect as rollback-only, which is what makes a statement that
     * never reached the database count the same as one that failed there. A table that was not found is
     * still a statement of the body that did not run, and committing the rest of them would be exactly
     * the half-finished transaction the section exists to prevent.
     */
    fun report(event: Event?, producer: RuntimeErrorProducer, message: String) {
        event?.let {
            ConnectionScope.transaction(it)?.markFailed(IllegalStateException(message))
            SkriptDatabaseErrors.set(it, message)
        }
        producer.error(message)
    }

    /**
     * Records a failure that came back from the database, the way [report] records one that never left.
     *
     * Both have to make the transaction in effect rollback-only. A failure the server reported is at
     * least as good a reason to undo the group: the server may have ended the transaction on its own
     * (PostgreSQL refuses every statement after an error until the transaction ends), and leaving the
     * transaction active would let the body carry on, a later statement clear the error slot, and the
     * body end by committing whatever the earlier statements wrote.
     *
     * Marking is only half of it: the statements after a failure are skipped because the transaction is
     * no longer active, which is what keeps a number they would have written from outliving them.
     */
    fun recordFailure(event: Event?, error: Throwable) {
        event?.let {
            ConnectionScope.transaction(it)?.markFailed(error)
            SkriptDatabaseErrors.set(it, error)
        }
    }

    /**
     * Reports a refusal on behalf of a read, after clearing the variable it would have stored into.
     *
     * A read that never ran must not leave the previous result in place. A script cannot tell a stale
     * variable from a fresh one, and the two mean opposite things: "the row is gone" against "the
     * statement did not run". Clearing first is the same rule the affected row count follows, and the
     * reason a read that failed at the database also clears.
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
     * Clears the event's error slot for a statement that is about to run, except inside a transaction.
     *
     * A transaction keeps the failure that made it rollback-only. The statements after that failure are
     * skipped without saying anything, which is deliberate, so clearing the slot here would leave the
     * script reading nothing at all and the body looking as if it had simply not been reached.
     */
    fun clearErrorForStatement(event: Event) {
        if (ConnectionScope.transaction(event) == null) {
            SkriptDatabaseErrors.clear(event)
        }
    }

    /**
     * Runs [block] on the connection a transaction pinned, or on one borrowed from the pool when the
     * statement runs on its own.
     *
     * The transaction is passed rather than looked up, because the scope it came from belongs to the
     * server thread while this runs on another one.
     */
    suspend fun <T> withQueries(
        database: Database,
        transaction: Transaction?,
        block: suspend (Queries) -> T
    ): T = transaction?.withQueries(block) ?: database.withQueries(block)

    /**
     * Runs [query] on the plugin's scope, then [deliver] on the server thread, and walks [continuation]
     * from there.
     *
     * The caller's `walk` returns null after calling this: the trigger is parked, its local variables are
     * taken away and put back around the query so that the script cannot tell the difference, and the
     * lines after the statement run once the work is done. Every statement in this addon waits, which is
     * what makes the order of a script the order of its statements and puts every failure in
     * `last database error` rather than only in the console.
     *
     * That is also why the return type is `Nothing?`: the value is always null, and it exists so that a
     * caller says what happens to the trigger in one line, `return DatabaseWork.run(...)`.
     *
     * [clearErrorOnSuccess] is false for the work that undoes something the script already knows failed:
     * rolling a failed transaction back succeeds, and clearing the slot on the way out would throw away
     * the only account of why the transaction was rolled back.
     */
    fun <T : Any> run(
        event: Event,
        continuation: TriggerItem?,
        query: suspend () -> T,
        deliver: (T) -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
        clearErrorOnSuccess: Boolean = true
    ): Nothing? {
        val localVariables = SkriptLocalVariables.remove(event)
        Delay.addDelayedEvent(event)

        SkriptOrm.ioScope.launch {
            var result: T? = null
            var failure: Throwable? = null
            try {
                result = query()
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
                        deliver(checkNotNull(result))
                    }

                    TriggerItem.walk(continuation, event)
                } finally {
                    SkriptLocalVariables.clear(event)
                }
            }
        }

        // The work is in the coroutine above: this returns to park the trigger, always.
        return null
    }
}
