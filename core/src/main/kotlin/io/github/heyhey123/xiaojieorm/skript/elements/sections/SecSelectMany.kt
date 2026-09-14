package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

class SecSelectMany : SecSelectBase() {

    companion object {
        init {
            Skript.registerSection(
                SecSelectMany::class.java,
                "select many [entities] from [table] %string% [and] store [the] [results] in %objects% [wait:and wait] [where:where (any:[neg:no] any|all:[neg:not] all)]"
            )
        }
    }

    override val tableNameIndex: Int = 0

    override val resultVarIndex: Int = 1

    override suspend fun executeQuery(
        queries: Queries,
        table: Table,
        whereClause: WhereClause?,
        extraArguments: Any?
    ): Map<String, Any?> {
        val result = linkedMapOf<String, Any?>()
        queries.selectMany(whereClause).execute(table).cursor.use { cursor ->
            var rowIndex = 1
            while (cursor.next()) {
                result["$rowIndex::__index"] = rowIndex
                table.columns.values.forEach { column ->
                    result["$rowIndex::${column.name}"] = cursor.get(column.name, column.type)
                }
                rowIndex++
            }
        }
        return result
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "select many from table $tableNameExpr and store in $resultVar${if (waitFlag) " and wait" else ""}${if (where != null) " where..." else ""}"

}
