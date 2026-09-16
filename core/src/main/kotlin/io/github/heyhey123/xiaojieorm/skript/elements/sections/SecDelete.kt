package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptSyntax
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Delete Entities")
@Description("Deletes rows, optionally with a positive limit and nested where block. Omitting where deletes all rows allowed by the implementation. With and wait, failures are available as the last database error; otherwise asynchronous failures are only logged.")
@Example(
    """
delete entities from table "users" with limit 10 and wait:
    where any:
        active = false
        age < 18
"""
)
@Since("1.0")
class SecDelete : SecWriteBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(addon, SecDelete::class.java, "delete [entities] from [table] %string% [with limit %integer%] [wait:and wait]")
        }
    }

    private var limitExpr: Expression<Int>? = null
    override val supportsWhere = true
    override val requiresValues = false

    @Suppress("UNCHECKED_CAST")
    override fun extractExtraParams(expressions: Array<out Expression<*>?>, matchedPattern: Int) {
        limitExpr = expressions[extraParamsIndex(matchedPattern)] as Expression<Int>?
    }

    override fun resolveExtraArguments(event: Event?): Any? = limitExpr?.getSingle(event)?.also {
        require(it > 0) { "Delete limit must be positive." }
    }

    override suspend fun executeWrite(queries: Queries, table: Table, singleValues: Map<String, Any?>?, multipleValues: List<Map<String, Any?>>?, whereClause: WhereClause?, extraArguments: Any?) {
        queries.delete(extraArguments as Int?, whereClause).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "delete entities from table $tableNameExpr"
}
