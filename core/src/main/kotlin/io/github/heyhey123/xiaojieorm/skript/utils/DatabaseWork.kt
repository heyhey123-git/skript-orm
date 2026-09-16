package io.github.heyhey123.xiaojieorm.skript.utils

import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Trigger
import ch.njol.skript.lang.TriggerItem
import io.github.heyhey123.xiaojieorm.XiaojieOrm
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
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

    /** The connection and the table a resolved statement works against. */
    class Target(val database: Database, val table: Table)

    /**
     * Resolves the table [tableNameExpr] names, reporting a failure the way the sections do.
     *
     * Returns null when the caller should let the trigger carry on with its next item instead; a message
     * for `last database error` has already been stored at that point.
     */
    fun resolveTable(event: Event, trigger: Trigger, tableNameExpr: Expression<String>): Target? {
        val database = Database.current ?: run {
            report(event, trigger, "No database connected.")
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

        if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) {
            report(event, trigger, "Database lifecycle is shutting down.")
            return null
        }

        return Target(database, table)
    }

    /** Reports [message] the way a section does: as the last database error, and in the console. */
    fun report(event: Event, trigger: Trigger, message: String) {
        SkriptDatabaseErrors.set(event, message)
        ErrorPrinter.printErrorMessageWithDetail(trigger, message)
    }

    /**
     * Runs [query] on the plugin's scope, then [deliver] on the server thread.
     *
     * With [wait], the trigger is parked and walked on from [continuation] once the work is done, and the
     * event's local variables are taken away and put back around the query so that the script cannot tell
     * the difference. Without it the work is left to the background and the trigger carries on at once,
     * which is what a write promises when `and wait` is left out: a failure is then only logged, and
     * `last database error` stays as it was.
     *
     * The return value is what the caller should return from its walk: null when the trigger has been
     * parked, [continuation] when it should carry on by itself.
     */
    fun <T : Any> run(
        event: Event,
        trigger: Trigger,
        continuation: TriggerItem?,
        wait: Boolean,
        query: suspend () -> T,
        deliver: (T) -> Unit = {},
        onFailure: (Throwable) -> Unit = {}
    ): TriggerItem? {
        if (!wait) {
            XiaojieOrm.ioScope.launch {
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

        XiaojieOrm.ioScope.launch {
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
                if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) return@withContext
                try {
                    if (localVariables != null) {
                        SkriptLocalVariables.restore(event, localVariables)
                    }

                    val error = failure
                    if (error != null) {
                        SkriptDatabaseErrors.set(event, error)
                        onFailure(error)
                    } else {
                        SkriptDatabaseErrors.clear(event)
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
