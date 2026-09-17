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
 * `update one ... by id` taking its values from a variable, written without a colon.
 *
 * @see EffInsertOneFromVariable for why this form exists beside its section.
 */
@Name("Update One Entity By ID From A Variable Without A Colon")
@Description("Updates one row by its registered primary-key value, taking the new values from a list variable shaped like a select result. Written without a colon, because a section with no body is what Skript warns about. Only the columns the variable holds are touched. With and wait, failures are available as the last database error.")
@Example(
    """
select one entity from table "users" and store the result in {_user::*}
set {_user::age} to {_user::age} + 1
update one entity {_user::*} in table "users" by id {_user::id} and wait
"""
)
@Since("1.0")
class EffUpdateByIdFromVariable : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffUpdateByIdFromVariable::class.java,
                "update [one] [entity] %objects% in [table] %string% by id %object% [wait:and wait]"
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
                "Failed to parse write arguments: ID expression in 'update by id' is null."
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
                    queries.updateById(id, row).execute(target.table)
                }
            },
            onFailure = { error ->
                ErrorPrinter.printErrorMessageWithDetail(trigger, "Write failed: ${error.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "update one $valuesVariable in table $tableNameExpr by id $idExpr"
}
