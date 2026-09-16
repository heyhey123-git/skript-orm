package io.github.heyhey123.xiaojieorm.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.skript.lang.Variable
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.XiaojieOrm
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptLocalVariables
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptSyntax
import io.github.heyhey123.xiaojieorm.skript.utils.VariableModifier
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * `select one` for the case that has no filter, written without a colon.
 *
 * A section has to be written with a colon, and Skript warns about every section with nothing indented
 * under it. There is nothing to indent when there is no `where` block, so the same statement is offered
 * as an effect for that case, and a script that wants a filter writes the section instead. Skript picks
 * between them by the node it read: a line ending in a colon is a section, a line without one is an
 * effect, so the two never compete even though their patterns match the same words.
 *
 * The work is done the way the sections do it, and for the same reasons: expressions are read on the
 * server thread, the query runs on the plugin's own scope, and the result is written back on the server
 * thread before the trigger carries on. `and wait` is accepted and ignored, because a read has nothing
 * to do until it has the rows.
 */
@Name("Select One Entity Without A Filter")
@Description("Selects at most one row and stores it by column name, such as {_user::name}, for the case with no where block. Written without a colon, because a section with no body is what Skript warns about. Use the section form when a filter is needed. Selects always wait and expose failures as the last database error.")
@Example(
    """
select one entity from table "users" and store the result in {_user::*}
send "name: %{_user::name}%"
"""
)
@Since("1.0")
class EffSelectOneUnfiltered : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffSelectOneUnfiltered::class.java,
                "select one [entity] from [table] %string% [and] store [the] [result] in %objects% [and wait]"
            )
        }
    }

    private lateinit var tableNameExpr: Expression<String>
    private lateinit var resultVar: Variable<*>

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        tableNameExpr = expressions[0] as Expression<String>

        val resultExpression = expressions[1] ?: return false
        if (resultExpression !is Variable<*>) {
            Skript.error("The result of a select must be stored in a list variable, such as {_user::*}.")
            return false
        }
        resultVar = resultExpression

        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    /**
     * Never called: the work happens in [walk], which is the only place that runs on the server thread and
     * can hand the trigger a continuation. `Effect` declares this abstract, so it has to exist.
     */
    override fun execute(event: Event?) = Unit

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return next
        val trigger = this.trigger ?: return next
        SkriptDatabaseErrors.clear(actualEvent)

        val database = Database.current ?: run {
            SkriptDatabaseErrors.set(actualEvent, "No database connected.")
            ErrorPrinter.printErrorMessageWithDetail(trigger, "No database connected.")
            return next
        }

        val tableName = tableNameExpr.getSingle(actualEvent) ?: run {
            SkriptDatabaseErrors.set(actualEvent, "Table name is null.")
            ErrorPrinter.printErrorMessageWithDetail(trigger, "Table name is null.")
            return next
        }

        val table = database.tables[tableName] ?: run {
            val message = "Table '$tableName' not found."
            SkriptDatabaseErrors.set(actualEvent, message)
            ErrorPrinter.printErrorMessageWithDetail(trigger, message)
            return next
        }

        if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) {
            SkriptDatabaseErrors.set(actualEvent, "Database lifecycle is shutting down.")
            ErrorPrinter.printErrorMessageWithDetail(trigger, "Database lifecycle is shutting down.")
            return next
        }

        val continuation = next
        val localVariables = SkriptLocalVariables.remove(actualEvent)
        Delay.addDelayedEvent(actualEvent)

        XiaojieOrm.ioScope.launch {
            var row: Map<String, Any?>? = null
            var failure: Throwable? = null
            try {
                row = database.withQueries { queries ->
                    val result = linkedMapOf<String, Any?>()
                    queries.selectOne(null).execute(table).cursor.use { cursor ->
                        if (cursor.next()) {
                            table.columns.values.forEach { column ->
                                result[column.name] = cursor.get(column.name, column.type)
                            }
                        }
                    }
                    result
                }
            } catch (_: CancellationException) {
                return@launch
            } catch (error: Throwable) {
                failure = error
            }

            withContext(NonCancellable + SyncDispatcher) {
                if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) return@withContext
                try {
                    if (localVariables != null) {
                        SkriptLocalVariables.restore(actualEvent, localVariables)
                    }

                    val queryFailure = failure
                    if (queryFailure != null) {
                        VariableModifier.clear(resultVar, actualEvent)
                        SkriptDatabaseErrors.set(actualEvent, queryFailure)
                        ErrorPrinter.printErrorMessageWithDetail(trigger, "Query failed: ${queryFailure.message}")
                    } else {
                        SkriptDatabaseErrors.clear(actualEvent)
                        VariableModifier.writeMap(resultVar, actualEvent, checkNotNull(row))
                    }

                    TriggerItem.walk(continuation, actualEvent)
                } finally {
                    SkriptLocalVariables.clear(actualEvent)
                }
            }
        }

        return null
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "select one from table $tableNameExpr and store the result in $resultVar"
}
