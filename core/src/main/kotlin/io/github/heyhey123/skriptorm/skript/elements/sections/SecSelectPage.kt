package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Select Page")
@Description("Selects a one-based page with positive size. Results use page-local row-index and column-name keys, even for one result. Pagination requires a registered primary key. Selects always wait and expose failures as the last database error.")
@Example(
    """select page 2 with size 20 from table "users" and store the results in {_page::*}:
    where all:
        active = true
send "%{_page::1::name}%"
"""
)
@Since("1.0.0")
class SecSelectPage : SecSelectBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecSelectPage::class.java,
                "select page %integer% [with] size %integer% from [table] %string% [and] store [the] [results] in %objects% [and wait]"
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

    override fun resolveExtraArguments(event: Event?): Any {
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
        if (where != null) append(" where...")
    }
}
