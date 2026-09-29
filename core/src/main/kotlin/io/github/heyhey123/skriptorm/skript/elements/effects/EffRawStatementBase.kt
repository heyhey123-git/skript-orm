package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.LiteralList
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
 * A raw statement is resolved against no table, so the only checks here are the ones it can honour: the
 * statement has to say something, and the number of values has to match the placeholders. See
 * [io.github.heyhey123.skriptorm.queries.RawStatement] for what that leaves unchecked.
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

        // The `with` clause is optional, so Skript pads the omitted slot with null rather than leaving it out;
        // its absence says nothing about whether it was written.
        //
        // A list written out of literals arrives as an unparsed `LiteralList`, which throws when its values are
        // read. A single value and a list variable both arrive readable, so a literal list is refused here.
        val parameters = parameterIndex(matchedPattern)
        val parameterSlot = if (parameters >= 0) expressions[parameters] else null
        if (parameterSlot != null) {
            if (parameterSlot is LiteralList<*>) {
                Skript.error(
                    "The values after 'with' must be one value or a list variable, such as {_values::*}, " +
                        "because a list written out of literals cannot be read by this statement."
                )
                return false
            }
            parameterExpr = ExpressionsHelper.withAnyType(parameterSlot)
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
