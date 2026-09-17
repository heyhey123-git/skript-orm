package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Trigger
import ch.njol.skript.lang.TriggerItem
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

        /**
         * Whether the statement has to wait for its work.
         *
         * Inside a transaction it always does. Two statements running at once on one pinned connection is
         * not something a script should be able to ask for, and a transaction cannot commit before the
         * statements it is made of have finished.
         */
        val mustWait: Boolean
            get() = transaction != null
    }

    /**
     * Resolves the table [tableNameExpr] names, reporting a failure the way the sections do.
     *
     * Returns null when the caller should let the trigger carry on with its next item instead; a message
     * for `last database error` has already been stored at that point.
     */
    fun resolveTable(event: Event, trigger: Trigger, tableNameExpr: Expression<String>): Target? {
        if (skipInactiveTransaction(event, trigger)) return null

        val database = ConnectionScope.resolve(event) ?: run {
            report(event, trigger, ConnectionScope.noConnectionMessage())
            return null
        }

        val tableName = tableNameExpr.getSingle(event) ?: run {
            report(event, trigger, "Table name is null.")
            return null
        }

        val table = database.tables[tableName] ?: run {
            report(event, trigger, "Table '$tableName' not found.")
            return null
        }

        if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) {
            report(event, trigger, "Database lifecycle is shutting down.")
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
    fun skipInactiveTransaction(event: Event, trigger: Trigger): Boolean {
        val transaction = ConnectionScope.transaction(event) ?: return false
        if (transaction.isActive) return false
        if (transaction.state != Transaction.State.ROLLBACK_ONLY) {
            report(
                event,
                trigger,
                transaction.failure?.message
                    ?: "The database transaction is ${transaction.state} and cannot run this statement."
            )
        }
        return true
    }

    /**
     * Reports [message] the way a section does: as the last database error, and in the console.
     *
     * It also marks the transaction in effect as rollback-only, which is what makes a statement that
     * never reached the database count the same as one that failed there. A table that was not found is
     * still a statement of the body that did not run, and committing the rest of them would be exactly
     * the half-finished transaction the section exists to prevent.
     */
    fun report(event: Event?, trigger: Trigger, message: String) {
        event?.let {
            ConnectionScope.transaction(it)?.markFailed(IllegalStateException(message))
            SkriptDatabaseErrors.set(it, message)
        }
        ErrorPrinter.printErrorMessageWithDetail(trigger, message)
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
     * Runs [query] on the plugin's scope, then [deliver] on the server thread.
     *
     * With [wait], the trigger is parked and walked on from [continuation] once the work is done, and the
     * event's local variables are taken away and put back around the query so that the script cannot tell
     * the difference. Without it the work is left to the background and the trigger carries on at once,
     * which is what a write promises when `and wait` is left out: a failure is then only logged, and
     * `last database error` stays as it was.
     *
     * [clearErrorOnSuccess] is false for the work that undoes something the script already knows failed:
     * rolling a failed transaction back succeeds, and clearing the slot on the way out would throw away
     * the only account of why the transaction was rolled back.
     *
     * The return value is what the caller should return from its walk: null when the trigger has been
     * parked, [continuation] when it should carry on by itself.
     */
    fun <T : Any> run(
        event: Event,
        continuation: TriggerItem?,
        wait: Boolean,
        query: suspend () -> T,
        deliver: (T) -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
        clearErrorOnSuccess: Boolean = true
    ): TriggerItem? {
        if (!wait) {
            SkriptOrm.ioScope.launch {
                try {
                    query()
                } catch (_: CancellationException) {
                } catch (error: Throwable) {
                    onFailure(error)
                }
            }
            return continuation
        }

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
                        SkriptDatabaseErrors.set(event, error)
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

        return null
    }
}
