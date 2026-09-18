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
import io.github.heyhey123.skriptorm.skript.utils.ErrorPrinter
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.skript.utils.VariableModifier
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * `select many` for the case with no filter, written without a colon.
 *
 * @see EffSelectOneUnfiltered for why this form exists beside its section.
 */
@Name("Select Many Entities Without A Filter")
@Description("Selects every row using row-index and column-name keys such as {_users::1::name}, for the case with no where block. Written without a colon, because a section with no body is what Skript warns about. Use the section form when a filter is needed. Selects always wait and expose failures as the last database error.")
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
        val trigger = this.trigger ?: return next
        DatabaseWork.clearErrorForStatement(actualEvent)

        val target = DatabaseWork.resolveTable(actualEvent, trigger, tableNameExpr) ?: return next

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            query = {
                target.withQueries { queries ->
                    val rows = linkedMapOf<String, Any?>()
                    queries.selectMany(null).execute(target.table).cursor.use { cursor ->
                        var rowIndex = 1
                        while (cursor.next()) {
                            target.table.columns.values.forEach { column ->
                                rows["$rowIndex::${column.name}"] = cursor.get(column.name, column.type)
                            }
                            rowIndex++
                        }
                    }
                    rows
                }
            },
            deliver = { rows -> VariableModifier.writeMap(resultVar, actualEvent, rows) },
            onFailure = { error ->
                VariableModifier.clear(resultVar, actualEvent)
                ErrorPrinter.printErrorMessageWithDetail(trigger, "Query failed: ${error.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "select many from table $tableNameExpr and store the results in $resultVar"
}
