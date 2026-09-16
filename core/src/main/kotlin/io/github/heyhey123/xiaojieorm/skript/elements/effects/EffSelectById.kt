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
import io.github.heyhey123.xiaojieorm.skript.utils.VariableModifier
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * `select ... by id`, written without a colon.
 *
 * This form never had a body to give: the row is named by its primary key and a `where` is refused, so the
 * section spelling was only ever a colon with nothing under it. It is offered as an effect for that
 * reason, and the section stays for scripts written before this one existed.
 */
@Name("Select Entity By ID Without A Colon")
@Description("Selects one row by its registered primary-key value and stores it by column name, such as {_user::name}. Written without a colon, because a section with no body is what Skript warns about. It does not accept where. Selects always wait and expose failures as the last database error.")
@Example(
    """
select entity from table "users" by id {_id} and store the result in {_user::*}
send "%{_user::name}%"
"""
)
@Since("1.0")
class EffSelectById : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffSelectById::class.java,
                "select [one] [entity] from [table] %string% by id %object% [and] store [the] [result] in %objects% [and wait]"
            )
        }
    }

    private lateinit var tableNameExpr: Expression<String>
    private lateinit var idExpr: Expression<Any>
    private lateinit var resultVar: Variable<*>

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        tableNameExpr = expressions[0] as Expression<String>
        idExpr = ExpressionsHelper.withAnyType(expressions[1]!!)

        val resultExpression = expressions[2] ?: return false
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
        SkriptDatabaseErrors.clear(actualEvent)

        val id = idExpr.getSingle(actualEvent)
        if (id == null) {
            DatabaseWork.report(
                actualEvent,
                trigger,
                "Failed to parse query arguments: ID expression in 'select by id' is null."
            )
            return next
        }

        val target = DatabaseWork.resolveTable(actualEvent, trigger, tableNameExpr) ?: return next

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            wait = true,
            query = {
                target.database.withQueries { queries ->
                    val row = linkedMapOf<String, Any?>()
                    queries.selectById(id).execute(target.table).cursor.use { cursor ->
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
        "select from table $tableNameExpr by id $idExpr and store the result in $resultVar"
}
