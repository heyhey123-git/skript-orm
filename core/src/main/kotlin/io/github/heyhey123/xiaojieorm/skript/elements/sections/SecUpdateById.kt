package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

class SecUpdateById : SecWriteBase() {
    companion object {
        init {
            Skript.registerSection(SecUpdateById::class.java, "update [one] [entity] in [table] %string% by id %object% [wait:and wait]")
        }
    }

    private lateinit var idExpr: Expression<Any>
    override val tableNameIndex = 0

    @Suppress("UNCHECKED_CAST")
    override fun extractExtraParams(expressions: Array<out Expression<*>?>) {
        idExpr = expressions[1] as Expression<Any>
    }

    override fun resolveExtraArguments(event: Event?): Any =
        requireNotNull(idExpr.getSingle(event)) { "ID expression in 'update by id' is null." }

    override suspend fun executeWrite(queries: Queries, table: Table, singleValues: Map<String, Any?>?, multipleValues: List<Map<String, Any?>>?, whereClause: WhereClause?, extraArguments: Any?) {
        queries.updateById(requireNotNull(extraArguments), requireNotNull(singleValues)).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "update entity in table $tableNameExpr by id $idExpr"
}
