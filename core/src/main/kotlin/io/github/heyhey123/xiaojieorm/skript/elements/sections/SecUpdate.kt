package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

class SecUpdate : SecWriteBase() {
    companion object {
        init {
            Skript.registerSection(SecUpdate::class.java, "update [entities] in [table] %string% [with limit %-integer%] [wait:and wait]")
        }
    }

    private var limitExpr: Expression<Int>? = null
    override val tableNameIndex = 0
    override val supportsWhere = true

    @Suppress("UNCHECKED_CAST")
    override fun extractExtraParams(expressions: Array<out Expression<*>?>) {
        limitExpr = expressions[1] as Expression<Int>?
    }

    override fun resolveExtraArguments(event: Event?): Any? = limitExpr?.getSingle(event)?.also {
        require(it > 0) { "Update limit must be positive." }
    }

    override suspend fun executeWrite(queries: Queries, table: Table, singleValues: Map<String, Any?>?, multipleValues: List<Map<String, Any?>>?, whereClause: WhereClause?, extraArguments: Any?) {
        queries.update(requireNotNull(singleValues), extraArguments as Int?, whereClause).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "update entities in table $tableNameExpr"
}
