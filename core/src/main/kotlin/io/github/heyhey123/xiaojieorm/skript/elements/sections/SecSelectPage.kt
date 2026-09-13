package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Trigger
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import io.github.heyhey123.xiaojieorm.skript.utils.VariableModifier
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
import kotlinx.coroutines.withContext
import org.bukkit.event.Event

class SecSelectPage : SecSelectBase() {

    companion object {
        init {
            Skript.registerSection(
                SecSelectPage::class.java,
                "select page %integer% [with] size %integer% from [table] %string% [and] store [the] [results] in %objects% [wait:and wait] [where:where (any:[neg:no] any|all:[neg:not] all)]"
            )
        }
    }

    lateinit var pageIndexExpr: Expression<Int>

    lateinit var pageSizeExpr: Expression<Int>

    override val tableNameIndex: Int = 2

    override val resultVarIndex: Int = 3

    @Suppress("UNCHECKED_CAST")
    override fun extractExtraParams(expressions: Array<out Expression<*>?>) {
        pageIndexExpr = expressions[0] as Expression<Int>
        pageSizeExpr = expressions[1] as Expression<Int>
    }

    override suspend fun executeQuery(
        database: Database,
        table: Table,
        whereClause: WhereClause?,
        event: Event?
    ) {
        val firstLine: Trigger = this.first!!.trigger!!

        val pageIndex = pageIndexExpr.getSingle(event) ?: let {
            ErrorPrinter.printErrorMessageWithDetail(
                firstLine,
                "Page index expression in 'select page' is null."
            )
            return
        }

        val pageSize = pageSizeExpr.getSingle(event) ?: let {
            ErrorPrinter.printErrorMessageWithDetail(
                firstLine,
                "Page size expression in 'select page' is null."
            )
            return
        }

        val result = mutableListOf<Map<String, Any?>>()
        val columns = table.columns.values
        database.queries!!
            .selectPage(pageSize, pageIndex, whereClause)
            .execute(table)
            .cursor
            .use { cursor ->
                while (cursor.next()) {
                    val row = mutableMapOf<String, Any?>()
                    columns.forEachIndexed { index, column ->
                        row[(index + 1).toString()] = cursor.get(column.name, column.type)
                    }
                    result.add(row)
                }
            }

        withContext(SyncDispatcher) {
            VariableModifier.writeList(resultVar, event, result)
        }
    }

    override fun toString(event: Event?, debug: Boolean): String = buildString {
        append("select page $pageIndexExpr with size $pageSizeExpr from table $tableNameExpr")
        append(" and store results in $resultVar")
        if (waitFlag) append(" and wait")
        if (where != null) append(" where...")
    }
}
