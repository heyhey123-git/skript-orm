package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.skript.lang.Variable
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.SelectResult
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.skript.utils.VariableModifier
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Select Many Entities Without A Filter")
@Description("Selects all rows into numbered list entries such as {_users::1::name}. Use this form without a colon; use the section form for a where block. Results over 5000 rows are rejected without storing a partial list. The next line runs after the query finishes. Check last database error for failures.")
@Example(
    """select many entities from table "users" and store the results in {_users::*}
send "first: %{_users::1::name}%"
"""
)
@Since("1.0.0")
class EffSelectManyUnfiltered : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffSelectManyUnfiltered::class.java,
                "select many [entities] from [table] %string% [and] store [the] [results] in %objects% [and wait]"
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
            Skript.error("The result of a select must be stored in a list variable, such as {_users::*}.")
            return false
        }
        resultVar = resultExpression

        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    /** Never called: the work is in [walk], the only place that runs on the server thread. */
    override fun execute(event: Event?) = Unit

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return next
        if (this.trigger == null) return next
        DatabaseWork.clearErrorForStatement(actualEvent)

        val target = DatabaseWork.resolveTable(actualEvent, this, tableNameExpr) ?: run {
            VariableModifier.clear(resultVar, actualEvent)
            return next
        }

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            resultVariable = resultVar,
            query = {
                target.withQueries { queries -> SelectResult.readMany(queries, target.table, null) }
            },
            deliver = { rows -> VariableModifier.writeMap(resultVar, actualEvent, rows) },
            onFailure = { failure ->
                VariableModifier.clear(resultVar, actualEvent)
                this.error("Query failed: ${failure.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "select many from table $tableNameExpr and store the results in $resultVar"
}
