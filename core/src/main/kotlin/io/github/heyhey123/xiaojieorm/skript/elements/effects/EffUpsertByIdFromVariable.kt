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
import io.github.heyhey123.xiaojieorm.skript.utils.ExpressionsHelper
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptSyntax
import io.github.heyhey123.xiaojieorm.skript.utils.WriteValues
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * `upsert one ... by id` taking its values from a variable, written without a colon.
 *
 * @see EffInsertOneFromVariable for why this form exists beside its section.
 */
@Name("Upsert One Entity By ID From A Variable Without A Colon")
@Description("Updates the row with the given primary-key value or inserts it when absent, taking the values from a list variable shaped like a select result. Written without a colon, because a section with no body is what Skript warns about. Support depends on the implementation. With and wait, failures are available as the last database error.")
@Example(
    """
set {_user::id} to 1
set {_user::name} to "Alice"
upsert one entity {_user::*} in table "users" by id {_user::id} and wait
"""
)
@Since("1.0")
class EffUpsertByIdFromVariable : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffUpsertByIdFromVariable::class.java,
                "upsert [one] [entity] %objects% in [table] %string% by id %object% [wait:and wait]"
            )
        }
    }

    private lateinit var tableNameExpr: Expression<String>
    private lateinit var idExpr: Expression<Any>
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
        idExpr = ExpressionsHelper.withAnyType(expressions[2]!!)
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

        val id = idExpr.getSingle(actualEvent)
        if (id == null) {
            DatabaseWork.report(
                actualEvent,
                trigger,
                "Failed to parse write arguments: ID expression in 'upsert by id' is null."
            )
            return next
        }

        val target = DatabaseWork.resolveTable(actualEvent, trigger, tableNameExpr) ?: return next

        val row = WriteValues.singleRow(actualEvent, trigger, valuesVariable, target) ?: return next

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            wait = waitFlag || target.mustWait,
            query = {
                target.withQueries { queries ->
                    queries.upsertById(id, row).execute(target.table)
                }
            },
            onFailure = { error ->
                ErrorPrinter.printErrorMessageWithDetail(trigger, "Write failed: ${error.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "upsert one $valuesVariable in table $tableNameExpr by id $idExpr"
}
