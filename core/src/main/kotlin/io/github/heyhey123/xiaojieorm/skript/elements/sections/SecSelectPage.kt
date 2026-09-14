package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Trigger
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
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

    private data class PageArguments(val pageIndex: Int, val pageSize: Int)

    override fun resolveExtraArguments(event: Event?, trigger: Trigger): Any {
        val pageIndex = requireNotNull(pageIndexExpr.getSingle(event)) {
            "Page index expression in 'select page' is null."
        }
        val pageSize = requireNotNull(pageSizeExpr.getSingle(event)) {
            "Page size expression in 'select page' is null."
        }
        require(pageIndex >= 1) { "Page index must be at least one." }
        require(pageSize > 0) { "Page size must be positive." }
        return PageArguments(pageIndex, pageSize)
    }

    override suspend fun executeQuery(
        queries: Queries,
        table: Table,
        whereClause: WhereClause?,
        extraArguments: Any?
    ): Map<String, Any?> {
        val arguments = extraArguments as PageArguments
        val pageIndex = arguments.pageIndex
        val pageSize = arguments.pageSize

        val result = linkedMapOf<String, Any?>()
        val columns = table.columns.values
        queries.selectPage(pageSize, pageIndex, whereClause)
            .execute(table)
            .cursor
            .use { cursor ->
                var rowIndex = 1
                while (cursor.next()) {
                    result["$rowIndex::__index"] = rowIndex
                    columns.forEach { column ->
                        result["$rowIndex::${column.name}"] = cursor.get(column.name, column.type)
                    }
                    rowIndex++
                }
            }
        return result
    }

    override fun toString(event: Event?, debug: Boolean): String = buildString {
        append("select page $pageIndexExpr with size $pageSizeExpr from table $tableNameExpr")
        append(" and store results in $resultVar")
        if (waitFlag) append(" and wait")
        if (where != null) append(" where...")
    }
}
