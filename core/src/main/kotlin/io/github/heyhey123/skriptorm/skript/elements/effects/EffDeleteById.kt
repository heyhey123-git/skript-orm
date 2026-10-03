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
import io.github.heyhey123.skriptorm.skript.utils.ExpressionsHelper
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Delete One Entity By ID Without A Colon")
@Description("Deletes the row with the given primary-key value. Use this form without a colon when no section body is needed. The next line runs after the delete finishes. Check last database error for failures or store affected rows for the reported count.")
@Example(
    """delete one entity from table "users" by id {_id} and wait
"""
)
@Since("1.0.0")
class EffDeleteById : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffDeleteById::class.java,
                "delete [one] [entity] from [table] %string% by id %object% [and store affected rows in %-number%] [wait:and wait]"
            )
        }
    }

    private lateinit var tableNameExpr: Expression<String>
    private lateinit var idExpr: Expression<Any>

    /** The variable the affected row count is stored in, or null when the statement did not ask for one. */
    private var affectedRowsVariable: Variable<*>? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        tableNameExpr = expressions[0] as Expression<String>
        idExpr = ExpressionsHelper.withAnyType(expressions[1]!!)
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
        if (this.trigger == null) return next
        DatabaseWork.clearErrorForStatement(actualEvent)
        AffectedRows.clear(affectedRowsVariable, actualEvent)

        val id = idExpr.getSingle(actualEvent)
        if (id == null) {
            DatabaseWork.report(
                actualEvent,
                this,
                "Failed to parse write arguments: ID expression in 'delete by id' is null."
            )
            return next
        }

        val target = DatabaseWork.resolveTable(actualEvent, this, tableNameExpr) ?: return next

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            query = {
                target.withQueries { queries -> queries.deleteById(id).execute(target.table) }
            },
            deliver = { result -> AffectedRows.write(affectedRowsVariable, actualEvent, result) },
            onFailure = { failure ->
                this.error("Write failed: ${failure.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "delete one from table $tableNameExpr by id $idExpr"
}
