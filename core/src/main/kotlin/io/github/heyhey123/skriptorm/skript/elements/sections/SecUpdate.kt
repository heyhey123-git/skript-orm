package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Update Entities")
@Description("Updates rows, optionally with a positive limit and nested where block. The new values may be written in the section body, or taken from a list variable shaped like a select result. Omitting where updates all rows allowed by the implementation. With and wait, failures are available as the last database error; otherwise asynchronous failures are only logged.")
@Example(
    """update entities in table "users" with limit 10 and wait:
    values:
        active: false
    where all:
        last_seen < {_cutoff}
        active = true
"""
)
@Example(
    """update entities {_changes::*} in table "users" and wait:
    where all:
        name = "Alice"
"""
)
@Since("1.0.0")
class SecUpdate : SecWriteBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecUpdate::class.java,
                "update [entities] in [table] %string% [with limit %integer%] [wait:and wait]",
                "update [entities] %objects% in [table] %string% [with limit %integer%] [wait:and wait]"
            )
        }
    }

    private var limitExpr: Expression<Int>? = null
    override val supportsWhere = true

    override fun valuesExpressionIndex(matchedPattern: Int) = if (matchedPattern == 0) -1 else 0

    @Suppress("UNCHECKED_CAST")
    override fun extractExtraParams(expressions: Array<out Expression<*>?>, matchedPattern: Int) {
        limitExpr = expressions[extraParamsIndex(matchedPattern)] as Expression<Int>?
    }

    override fun resolveExtraArguments(event: Event?): Any? = limitExpr?.getSingle(event)?.also {
        require(it > 0) { "Update limit must be positive." }
    }

    override suspend fun executeWrite(queries: Queries, table: Table, singleValues: Map<String, Any?>?, multipleValues: List<Map<String, Any?>>?, whereClause: WhereClause?, extraArguments: Any?) {
        // Patch semantics: the query layer builds the SET list from the supplied columns only, so a
        // column the values omit keeps its stored value. A list variable cannot carry a null, so
        // storing SQL NULL requires a literal `null` in a values block. Reading an omitted column as
        // NULL would silently clear every column a dynamic variable happens to miss.
        queries.update(requireNotNull(singleValues), extraArguments as Int?, whereClause).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "update entities in table $tableNameExpr"
}
