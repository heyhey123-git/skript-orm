package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.ErrorPrinter
import io.github.heyhey123.skriptorm.skript.utils.ExpressionsHelper
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * `delete one ... by id`, written without a colon.
 *
 * This form never had a body to give: the row is named by its primary key. It is offered as an effect for
 * that reason, and the section stays for scripts written before this one existed.
 */
@Name("Delete One Entity By ID Without A Colon")
@Description("Deletes one row by its registered primary-key value. Written without a colon, because a section with no body is what Skript warns about. With and wait, failures are available as the last database error; otherwise execution continues immediately and asynchronous failures are only logged.")
@Example(
    """
delete one entity from table "users" by id {_id} and wait
"""
)
@Since("1.0.0")
class EffDeleteById : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffDeleteById::class.java,
                "delete [one] [entity] from [table] %string% by id %object% [wait:and wait]"
            )
        }
    }

    private lateinit var tableNameExpr: Expression<String>
    private lateinit var idExpr: Expression<Any>
    private var waitFlag: Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        tableNameExpr = expressions[0] as Expression<String>
        idExpr = ExpressionsHelper.withAnyType(expressions[1]!!)
        waitFlag = parseResult.hasTag("wait")

        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    /** Never called: the work is in [walk], the only place that runs on the server thread. */
    override fun execute(event: Event?) = Unit

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return next
        val trigger = this.trigger ?: return next
        DatabaseWork.clearErrorForStatement(actualEvent)

        val id = idExpr.getSingle(actualEvent)
        if (id == null) {
            DatabaseWork.report(
                actualEvent,
                trigger,
                "Failed to parse write arguments: ID expression in 'delete by id' is null."
            )
            return next
        }

        val target = DatabaseWork.resolveTable(actualEvent, trigger, tableNameExpr) ?: return next

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            wait = waitFlag || target.mustWait,
            query = {
                target.withQueries { queries -> queries.deleteById(id).execute(target.table) }
            },
            onFailure = { error ->
                ErrorPrinter.printErrorMessageWithDetail(trigger, "Write failed: ${error.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "delete one from table $tableNameExpr by id $idExpr"
}
