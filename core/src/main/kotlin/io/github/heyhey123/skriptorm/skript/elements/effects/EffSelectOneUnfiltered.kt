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
 * `select one` for the case with no filter, written without a colon.
 *
 * A section needs a colon, and Skript warns about every section with nothing indented under it. There is
 * nothing to indent when there is no `where` block, so the statement is offered as an effect for that
 * case; a script that wants a filter writes the section instead. Skript picks between the two by the node
 * it read, so they never compete even though their patterns match the same words.
 */
@Name("Select One Entity Without A Filter")
@Description("Selects at most one row and stores it by column name, such as {_user::name}, for the case with no where block. Written without a colon, because a section with no body is what Skript warns about. Use the section form when a filter is needed. Selects always wait and expose failures as the last database error.")
@Example(
    """select one entity from table "users" and store the result in {_user::*}
send "name: %{_user::name}%"
"""
)
@Since("1.0.0")
class EffSelectOneUnfiltered : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffSelectOneUnfiltered::class.java,
                "select one [entity] from [table] %string% [and] store [the] [result] in %objects% [and wait]"
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
            Skript.error("The result of a select must be stored in a list variable, such as {_user::*}.")
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

        val target = DatabaseWork.resolveTable(actualEvent, trigger, tableNameExpr) ?: run {
            VariableModifier.clear(resultVar, actualEvent)
            return next
        }

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            query = {
                target.withQueries { queries ->
                    val row = linkedMapOf<String, Any?>()
                    queries.selectOne(null).execute(target.table).cursor.use { cursor ->
                        if (cursor.next()) {
                            target.table.columns.values.forEach { column ->
                                row[column.name] = cursor.get(column.name, column.type)
                            }
                        }
                    }
                    row
                }
            },
            deliver = { row -> VariableModifier.writeMap(resultVar, actualEvent, row) },
            onFailure = { error ->
                VariableModifier.clear(resultVar, actualEvent)
                ErrorPrinter.printErrorMessageWithDetail(trigger, "Query failed: ${error.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "select one from table $tableNameExpr and store the result in $resultVar"
}
