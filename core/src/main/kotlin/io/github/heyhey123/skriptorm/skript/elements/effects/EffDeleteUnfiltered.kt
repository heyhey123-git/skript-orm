package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.ErrorPrinter
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * `delete entities` without a `where`, written without a colon.
 *
 * A delete that keeps no rows back has no condition to name, so there is nothing to put in a body, and a
 * section without one is what Skript warns about. The section keeps its colon form, which is still the
 * way to write one that carries a `where` block.
 */
@Name("Delete Entities Without A Colon")
@Description("Deletes rows, optionally with a positive limit, without a where block: every row the implementation allows is deleted. Written without a colon, because a section with no body is what Skript warns about. With and wait, failures are available as the last database error; otherwise execution continues immediately and asynchronous failures are only logged.")
@Example(
    """
delete entities from table "logs" with limit 500 and wait
"""
)
@Since("1.0.0")
class EffDeleteUnfiltered : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffDeleteUnfiltered::class.java,
                "delete [entities] from [table] %string% [with limit %integer%] [wait:and wait]"
            )
        }
    }

    private lateinit var tableNameExpr: Expression<String>
    private var limitExpr: Expression<Int>? = null
    private var waitFlag: Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        tableNameExpr = expressions[0] as Expression<String>
        limitExpr = expressions[1] as Expression<Int>?
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

        val limit = limitExpr?.getSingle(actualEvent)
        if (limit != null && limit <= 0) {
            DatabaseWork.report(
                actualEvent,
                trigger,
                "Failed to parse write arguments: Delete limit must be positive."
            )
            return next
        }

        val target = DatabaseWork.resolveTable(actualEvent, trigger, tableNameExpr) ?: return next

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            wait = waitFlag || target.mustWait,
            query = {
                target.withQueries { queries -> queries.delete(limit, null).execute(target.table) }
            },
            onFailure = { error ->
                ErrorPrinter.printErrorMessageWithDetail(trigger, "Write failed: ${error.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "delete entities from table $tableNameExpr"
}
