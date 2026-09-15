package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

@Name("Update Entity By ID")
@Description("Updates one row by its registered primary-key value. The new values may be written in the section body, or taken from a list variable shaped like a select result. With and wait, failures are available as the last database error; otherwise asynchronous failures are only logged.")
@Example(
    """
update one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
"""
)
@Example(
    """
update one entity {_changes::*} in table "users" by id {_id}
"""
)
@Since("1.0")
class SecUpdateById : SecWriteBase() {

    companion object {
        init {
            Skript.registerSection(
                SecUpdateById::class.java,
                "update [one] [entity] in [table] %string% by id %object% [wait:and wait]",
                "update [one] [entity] %objects% in [table] %string% by id %object% [wait:and wait]"
            )
        }
    }

    private lateinit var idExpr: Expression<Any>

    override fun valuesExpressionIndex(matchedPattern: Int) = if (matchedPattern == 0) -1 else 0

    @Suppress("UNCHECKED_CAST")
    override fun extractExtraParams(expressions: Array<out Expression<*>?>, matchedPattern: Int) {
        idExpr = expressions[extraParamsIndex(matchedPattern)] as Expression<Any>
    }

    override fun resolveExtraArguments(event: Event?): Any =
        requireNotNull(idExpr.getSingle(event)) { "ID expression in 'update by id' is null." }

    override suspend fun executeWrite(queries: Queries, table: Table, singleValues: Map<String, Any?>?, multipleValues: List<Map<String, Any?>>?, whereClause: WhereClause?, extraArguments: Any?) {
        // Patch semantics, as in SecUpdate: only the supplied columns form the SET list, and an
        // omitted column is left untouched rather than written as NULL.
        queries.updateById(requireNotNull(extraArguments), requireNotNull(singleValues)).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "update entity in table $tableNameExpr by id $idExpr"
}
