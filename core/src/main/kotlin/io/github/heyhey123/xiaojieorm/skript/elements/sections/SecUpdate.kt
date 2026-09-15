package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

@Name("Update Entities")
@Description("Updates rows, optionally with a positive limit and nested where block. Omitting where updates all rows allowed by the implementation. With and wait, failures are available as the last database error; otherwise asynchronous failures are only logged.")
@Example("""
update entities in table "users" with limit 10 and wait:
    values:
        active: false
    where all:
        last_seen < {_cutoff}
        active = true
""")
@Since("1.0")
class SecUpdate : SecWriteBase() {
    companion object {
        init {
            Skript.registerSection(SecUpdate::class.java, "update [entities] in [table] %string% [with limit %integer%] [wait:and wait]")
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
