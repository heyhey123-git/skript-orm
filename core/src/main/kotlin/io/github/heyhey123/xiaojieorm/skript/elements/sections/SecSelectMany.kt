package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.skript.utils.VariableModifier
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
        database: Database,
        table: Table,
        whereClause: WhereClause?,
        event: Event?
    ) {
        val cursor = database.queries!!.selectMany(whereClause).execute(table).cursor
        val result = mutableListOf<Any?>()

        while (cursor.next()) {
            table.columns.values.forEach { column ->
                result.add(cursor.get(column.name, column.type))
            }
        }

        VariableModifier.writeList(resultVar, event, result)
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "select many from table $tableNameExpr and store in $resultVar${if (waitFlag) " and wait" else ""}${if (where != null) " where..." else ""}"

}
