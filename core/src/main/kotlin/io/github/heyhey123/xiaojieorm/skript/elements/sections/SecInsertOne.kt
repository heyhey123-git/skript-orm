package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

class SecInsertOne : SecWriteBase() {
    companion object {
        init {
            Skript.registerSection(SecInsertOne::class.java, "insert one [entity] into [table] %string% [wait:and wait]")
        }
    }

    override val tableNameIndex = 0

    override suspend fun executeWrite(
        queries: Queries,
        table: Table,
        singleValues: Map<String, Any?>?,
        multipleValues: List<Map<String, Any?>>?,
        whereClause: WhereClause?,
        extraArguments: Any?
    ) {
        queries.insertOne(requireNotNull(singleValues)).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "insert one into table $tableNameExpr"
}
