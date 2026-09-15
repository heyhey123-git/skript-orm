package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.lang.Expression
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

class SecSelectById : SecSelectBase() {
    companion object {
        init {
            Skript.registerSection(
                SecSelectById::class.java,
                "select [one] [entity] from [table] %string% by id %object% [and] store [the] [result] in %objects% [and wait]"
            )
        }
    }

    private lateinit var idExpr: Expression<Any>

    override val tableNameIndex = 0
    override val resultVarIndex = 2
    override val supportsWhere = false

    @Suppress("UNCHECKED_CAST")
    override fun extractExtraParams(expressions: Array<out Expression<*>?>) {
        idExpr = expressions[1] as Expression<Any>
    }

    override fun resolveExtraArguments(event: Event?, trigger: ch.njol.skript.lang.Trigger): Any =
        requireNotNull(idExpr.getSingle(event)) { "ID expression in 'select by id' is null." }

    override suspend fun executeQuery(
        queries: Queries,
        table: Table,
        whereClause: WhereClause?,
        extraArguments: Any?
    ): Map<String, Any?> {
        val result = linkedMapOf<String, Any?>()
        queries.selectById(requireNotNull(extraArguments)).execute(table).cursor.use { cursor ->
            if (cursor.next()) {
                table.columns.values.forEach { column ->
                    result[column.name] = cursor.get(column.name, column.type)
                }
            }
        }
        return result
    }

    override fun toString(event: Event?, debug: Boolean) =
        "select from table $tableNameExpr by id $idExpr and store the result in $resultVar"
}
