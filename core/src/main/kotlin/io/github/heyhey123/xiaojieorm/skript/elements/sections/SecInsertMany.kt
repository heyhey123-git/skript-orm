package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

class SecInsertMany : SecWriteBase() {
    companion object {
        init {
            Skript.registerSection(SecInsertMany::class.java, "insert many [entities] into [table] %string% [wait:and wait]")
        }
    }

    override val tableNameIndex = 0
    override val supportsMultipleRows = true

    override suspend fun executeWrite(
        queries: Queries,
        table: Table,
        singleValues: Map<String, Any?>?,
        multipleValues: List<Map<String, Any?>>?,
        whereClause: WhereClause?,
        extraArguments: Any?
    ) {
        queries.insertMany(multipleValues ?: listOf(requireNotNull(singleValues))).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "insert many into table $tableNameExpr"
}
