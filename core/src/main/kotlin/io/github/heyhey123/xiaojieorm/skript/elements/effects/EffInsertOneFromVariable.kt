package io.github.heyhey123.xiaojieorm.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.skript.lang.Variable
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.skript.utils.DatabaseWork
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptSyntax
import io.github.heyhey123.xiaojieorm.skript.utils.WriteValues
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * `insert one` taking its values from a variable, written without a colon.
 *
 * The rows come from the variable, so there is nothing to put in a body, and a section without one is what
 * Skript warns about. The `values` spelling keeps its section, because it has a body to give.
 */
@Name("Insert One Entity From A Variable Without A Colon")
@Description("Inserts one row, taking its values from a list variable shaped like a select result. Written without a colon, because a section with no body is what Skript warns about. With and wait, failures are available as the last database error; otherwise execution continues immediately and asynchronous failures are only logged.")
@Example(
    """
select one entity from table "users" and store the result in {_user::*}
insert one {_user::*} into table "archived_users"
"""
)
@Since("1.0")
class EffInsertOneFromVariable : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffInsertOneFromVariable::class.java,
                "insert one [entity] %objects% into [table] %string% [wait:and wait]"
            )
        }
    }

    private lateinit var tableNameExpr: Expression<String>
    private lateinit var valuesVariable: Variable<*>
    private var waitFlag: Boolean = false

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

        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    /** Never called: the work is in [walk], the only place that runs on the server thread. */
    override fun execute(event: Event?) = Unit

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return next
        val trigger = this.trigger ?: return next
        SkriptDatabaseErrors.clear(actualEvent)

        val target = DatabaseWork.resolveTable(actualEvent, trigger, tableNameExpr) ?: return next

        val row = WriteValues.singleRow(actualEvent, trigger, valuesVariable, target) ?: return next

        return DatabaseWork.run(
            event = actualEvent,
            trigger = trigger,
            continuation = next,
            wait = waitFlag,
            query = {
                target.database.withQueries { queries -> queries.insertOne(row).execute(target.table) }
            },
            onFailure = { error ->
                ErrorPrinter.printErrorMessageWithDetail(trigger, "Write failed: ${error.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "insert one $valuesVariable into table $tableNameExpr"
}
