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
import io.github.heyhey123.skriptorm.skript.utils.RowLimit
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.skript.utils.VariableModifier
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Select Page Without A Filter")
@Description("Selects an unfiltered page by its one-based page number and positive page size. Results are numbered from 1 within the page, such as {_users::1::name}. The table needs a registered primary key, and a page may contain at most 5000 rows. Use this form without a colon; use the section form for a where block.")
@Example(
    """select page 2 with size 20 from table "users" and store the results in {_page::*}
send "%{_page::1::name}%"
"""
)
@Since("1.0.0")
class EffSelectPageUnfiltered : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffSelectPageUnfiltered::class.java,
                "select page %integer% [with] size %integer% from [table] %string% [and] store [the] [results] in %objects% [and wait]"
            )
        }
    }

    private lateinit var pageIndexExpr: Expression<Int>
    private lateinit var pageSizeExpr: Expression<Int>
    private lateinit var tableNameExpr: Expression<String>
    private lateinit var resultVar: Variable<*>

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        pageIndexExpr = expressions[0] as Expression<Int>
        pageSizeExpr = expressions[1] as Expression<Int>
        tableNameExpr = expressions[2] as Expression<String>

        val resultExpression = expressions[3] ?: return false
        if (resultExpression !is Variable<*>) {
            Skript.error("The result of a select must be stored in a list variable, such as {_page::*}.")
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

        val pageIndex = pageIndexExpr.getSingle(actualEvent)
        val pageSize = pageSizeExpr.getSingle(actualEvent)
        when {
            pageIndex == null -> {
                DatabaseWork.refuseRead(actualEvent, this, "Failed to parse query arguments: Page index expression in 'select page' is null.", resultVar)
                return next
            }

            pageSize == null -> {
                DatabaseWork.refuseRead(actualEvent, this, "Failed to parse query arguments: Page size expression in 'select page' is null.", resultVar)
                return next
            }

            pageIndex < 1 -> {
                DatabaseWork.refuseRead(actualEvent, this, "Failed to parse query arguments: Page index must be at least one.", resultVar)
                return next
            }

            pageSize <= 0 -> {
                DatabaseWork.refuseRead(actualEvent, this, "Failed to parse query arguments: Page size must be positive.", resultVar)
                return next
            }

            // A page is the whole of what this statement stores, so a page larger than a statement may
            // store is known here, before anything is sent. RowLimit owns the ceiling and the wording.
            pageSize > RowLimit.ROWS -> {
                DatabaseWork.refuseRead(actualEvent, this, RowLimit.pageRefusal(pageSize), resultVar)
                return next
            }
        }

        val target = DatabaseWork.resolveTable(actualEvent, this, tableNameExpr) ?: run {
            VariableModifier.clear(resultVar, actualEvent)
            return next
        }

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            query = {
                target.withQueries { queries ->
                    val rows = linkedMapOf<String, Any?>()
                    queries.selectPage(pageSize, pageIndex, null).execute(target.table).cursor.use { cursor ->
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
            onFailure = { failure ->
                VariableModifier.clear(resultVar, actualEvent)
                this.error("Query failed: ${failure.message}")
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "select page $pageIndexExpr with size $pageSizeExpr from table $tableNameExpr and store the results in $resultVar"
}
