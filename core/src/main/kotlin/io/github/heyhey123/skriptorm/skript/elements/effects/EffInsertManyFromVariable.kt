package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.skript.lang.Variable
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.queries.insertManyInStatements
import io.github.heyhey123.skriptorm.skript.utils.AffectedRows
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.skript.utils.WriteValues
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Insert Many Entities From A Variable Without A Colon")
@Description("Inserts rows from a list variable in select-result format. Large batches are split across statements. Use this form without a colon when no section body is needed. The next line runs after the insert finishes. Check last database error for failures or store affected rows for the reported count.")
@Example(
    """select many entities from table "users" and store the results in {_rows::*}
insert many {_rows::*} into table "archived_users" and wait
"""
)
@Since("1.0.0")
class EffInsertManyFromVariable : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffInsertManyFromVariable::class.java,
                "insert many [entities] %objects% into [table] %string% [and store affected rows in %-number%] [wait:and wait]"
            )
        }
    }

    private lateinit var tableNameExpr: Expression<String>
    private lateinit var valuesVariable: Variable<*>

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
            Skript.error("The values of a write must come from a list variable, such as {_rows::*}.")
            return false
        }
        valuesVariable = valuesExpression
        tableNameExpr = expressions[1] as Expression<String>
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

        val target = DatabaseWork.resolveTable(actualEvent, this, tableNameExpr) ?: return next

        val rows = WriteValues.rows(actualEvent, this, valuesVariable, target) ?: return next

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            query = {
                target.withQueries { queries -> queries.insertManyInStatements(target.table, rows) }
            },
            deliver = { result -> AffectedRows.write(affectedRowsVariable, actualEvent, result) },
            onFailure = { failure ->
                this.error("Write failed: ${failure.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "insert many $valuesVariable into table $tableNameExpr"
}
