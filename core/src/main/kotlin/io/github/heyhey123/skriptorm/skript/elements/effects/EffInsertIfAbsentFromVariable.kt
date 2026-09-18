package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.skript.lang.Variable
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.skript.utils.AffectedRows
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.ErrorPrinter
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.skript.utils.WriteValues
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * `insert ... if absent` taking its values from a variable, written without a colon.
 *
 * @see EffInsertOneFromVariable for why this form exists beside its section.
 */
@Name("Insert Entity If Absent From A Variable Without A Colon")
@Description("Inserts one row only when the database implementation considers it absent, taking its values from a list variable shaped like a select result. Written without a colon, because a section with no body is what Skript warns about. Support and conflict rules depend on the implementation. With and wait, failures are available as the last database error. The store affected rows clause keeps the number of rows the statement affected.")
@Example(
    """insert entity {_user::*} if absent into table "users" and wait
"""
)
@Since("1.0.0")
class EffInsertIfAbsentFromVariable : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffInsertIfAbsentFromVariable::class.java,
                "insert [one] [entity] %objects% if absent into [table] %string% [and store affected rows in %-number%] [wait:and wait]"
            )
        }
    }

    private lateinit var tableNameExpr: Expression<String>
    private lateinit var valuesVariable: Variable<*>
    private var waitFlag: Boolean = false

    /** The variable the affected row count is stored in, or null when the statement did not ask for one. */
    private var affectedRowsVariable: Variable<*>? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        val valuesExpression = expressions[0] ?: return false
        if (valuesExpression !is Variable<*> || !valuesExpression.isList) {
            Skript.error("The values of a write must come from a list variable, such as {_row::*}.")
            return false
        }
        valuesVariable = valuesExpression
        tableNameExpr = expressions[1] as Expression<String>
        waitFlag = parseResult.hasTag("wait")
        // The count clause is the last expression of the pattern, so it is the last slot.
        affectedRowsVariable = try {
            AffectedRows.target(expressions.lastOrNull())
        } catch (error: IllegalArgumentException) {
            Skript.error(error.message ?: "Invalid affected row count target.")
            return false
        }

        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    /** Never called: the work is in [walk], the only place that runs on the server thread. */
    override fun execute(event: Event?) = Unit

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return next
        val trigger = this.trigger ?: return next
        DatabaseWork.clearErrorForStatement(actualEvent)
        AffectedRows.clear(affectedRowsVariable, actualEvent)

        val target = DatabaseWork.resolveTable(actualEvent, trigger, tableNameExpr) ?: return next

        val row = WriteValues.singleRow(actualEvent, trigger, valuesVariable, target) ?: return next

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            wait = waitFlag || target.mustWait,
            query = {
                target.withQueries { queries -> queries.insertIfAbsent(row).execute(target.table) }
            },
            deliver = { result -> AffectedRows.write(affectedRowsVariable, actualEvent, result) },
            onFailure = { error ->
                ErrorPrinter.printErrorMessageWithDetail(trigger, "Write failed: ${error.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "insert $valuesVariable if absent into table $tableNameExpr"
}
