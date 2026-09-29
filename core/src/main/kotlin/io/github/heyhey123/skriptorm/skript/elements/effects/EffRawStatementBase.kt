package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.skript.lang.Variable
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.SkriptOrm
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.skript.utils.AffectedRows
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.ExpressionsHelper
import io.github.heyhey123.skriptorm.skript.utils.SkriptLocalVariables
import io.github.heyhey123.skriptorm.utils.SyncDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event

/**
 * What the raw statement effects share: the statement, its values, the guards before it is sent, and the
 * hand-off that runs it off the server thread.
 *
 * A raw statement is not resolved against a table — there is no declaration behind it — so this walks the
 * steps a declared write does minus the table: clear the error slot, read the statement and its values on
 * the server thread, refuse what can be refused before anything is sent, resolve the connection, run, then
 * report the outcome and walk the continuation.
 *
 * The guards are the ones a raw statement can actually honour: the statement has to say something, and the
 * number of values has to match the number of placeholders. Nothing else is checked, because nothing else
 * can be — see [io.github.heyhey123.skriptorm.queries.RawStatement]. Which raw form this connection takes is
 * decided by its own query factory, so a statement of the wrong shape is refused by the backend with the
 * alternative named, rather than by a list of backends kept here.
 */
abstract class EffRawStatementBase : Effect() {

    protected lateinit var statementExpr: Expression<String>
    private var parameterExpr: Expression<Any>? = null
    protected var affectedRowsVariable: Variable<*>? = null

    /** The parameter slot's index in the matched pattern, or -1 when this pattern has no parameters. */
    protected abstract fun parameterIndex(matchedPattern: Int): Int

    /**
     * Whether this pattern ends with the affected-rows clause.
     *
     * A statement that stores rows instead of a count ends with its result variable, and reading that slot
     * as a count target would refuse the statement for storing what it was asked to store.
     */
    protected open val storesAffectedRows: Boolean = true

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        statementExpr = expressions[0] as Expression<String>

        val parameters = parameterIndex(matchedPattern)
        if (parameters >= 0) {
            parameterExpr = ExpressionsHelper.withAnyType(expressions[parameters]!!)
        }

        if (storesAffectedRows) {
            // The count clause is the last expression of the pattern, so it is the last slot.
            affectedRowsVariable = try {
                AffectedRows.target(expressions.lastOrNull())
            } catch (error: IllegalArgumentException) {
                Skript.error(error.message ?: "Invalid affected row count target.")
                return false
            }
        }

        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    override fun execute(event: Event?) = Unit

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return next
        if (this.trigger == null) return next
        DatabaseWork.clearErrorForStatement(actualEvent)

        val statement = statementOf(actualEvent) ?: return next
        val parameters = parametersOf(actualEvent, statement) ?: return next
        val target = DatabaseWork.resolveConnection(actualEvent, this) ?: return next

        AffectedRows.clear(affectedRowsVariable, actualEvent)

        val continuation = next
        val localVariables = SkriptLocalVariables.remove(actualEvent)
        val triggerItem = this
        SkriptOrm.ioScope.launch {
            var failure: Throwable? = null
            var result: Any? = null
            try {
                result = target.withQueries { queries ->
                    executeStatement(queries, statement, parameters)
                }
            } catch (_: CancellationException) {
                return@launch
            } catch (error: Throwable) {
                failure = error
            }

            withContext(NonCancellable + SyncDispatcher) {
                if (!SkriptOrm.instance.isEnabled) return@withContext
                try {
                    if (localVariables != null) SkriptLocalVariables.restore(actualEvent, localVariables)

                    val rawFailure = failure
                    if (rawFailure != null) {
                        DatabaseWork.recordFailure(actualEvent, rawFailure)
                        triggerItem.error("Statement failed: ${rawFailure.message}")
                    } else {
                        DatabaseWork.clearErrorForStatement(actualEvent)
                        deliver(actualEvent, result)
                    }

                    TriggerItem.walk(continuation, actualEvent)
                } finally {
                    SkriptLocalVariables.clear(actualEvent)
                }
            }
        }

        return null
    }

    /** The text the statement was written with, refused when it says nothing. */
    private fun statementOf(event: Event): String? {
        val statement = statementExpr.getSingle(event)?.trim()
        if (statement.isNullOrEmpty()) {
            DatabaseWork.report(event, this, "The raw statement is empty.")
            return null
        }
        return statement
    }

    /**
     * The values to bind, refused when their number does not match the placeholders in [statement].
     *
     * The placeholders are counted as the `?` characters in the statement, which is the rule a script can
     * apply by reading its own statement: this addon does not parse SQL, so a `?` inside a string literal or
     * a comment counts here exactly as the driver will not count it. That mismatch is reported by the
     * driver, in its own words, which is the same account a script gets for every other mistake in a
     * statement it wrote itself.
     */
    private fun parametersOf(event: Event, statement: String): List<Any?>? {
        val values = parameterExpr?.getAll(event)?.toList() ?: emptyList()
        val placeholders = statement.count { it == '?' }
        if (placeholders != values.size) {
            DatabaseWork.report(
                event,
                this,
                "The statement has $placeholders parameter placeholder(s) ('?'), but ${values.size} " +
                    "value(s) were given."
            )
            return null
        }
        return values
    }

    /**
     * Runs the statement and answers what this effect stores.
     *
     * [Queries] is the backend's own factory, so which raw form exists here is decided by the connection
     * and not by this element.
     */
    protected abstract suspend fun executeStatement(
        queries: Queries,
        statement: String,
        parameters: List<Any?>
    ): Any?

    /** Stores what the statement answered. Called on the server thread, with the event's variables back. */
    protected abstract fun deliver(event: Event, result: Any?)
}
