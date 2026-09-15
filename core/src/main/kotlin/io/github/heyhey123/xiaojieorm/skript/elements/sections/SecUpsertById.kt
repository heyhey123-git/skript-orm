package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

@Name("Upsert Entity By ID")
@Description("Updates the row with the given primary-key value or inserts it when absent. The values may be written in the section body, or taken from a list variable shaped like a select result. Support depends on the implementation. With and wait, failures are available as the last database error; otherwise asynchronous failures are only logged.")
@Example(
    """
upsert one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
"""
)
@Example(
    """
upsert one entity {_user::*} in table "users" by id {_id}
"""
)
@Since("1.0")
class SecUpsertById : SecWriteBase() {

    companion object {
        init {
            Skript.registerSection(
                SecUpsertById::class.java,
                "upsert [one] [entity] in [table] %string% by id %object% [wait:and wait]",
                "upsert [one] [entity] %objects% in [table] %string% by id %object% [wait:and wait]"
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
        requireNotNull(idExpr.getSingle(event)) { "ID expression in 'upsert by id' is null." }

    override suspend fun executeWrite(queries: Queries, table: Table, singleValues: Map<String, Any?>?, multipleValues: List<Map<String, Any?>>?, whereClause: WhereClause?, extraArguments: Any?) {
        // The supplied columns feed both branches of the statement: an absent row is inserted with
        // only them, so its other columns fall back to the database default, and an existing row is
        // patched with only them, so its other columns keep their stored value. A column the values
        // omit therefore never becomes NULL in either branch.
        queries.upsertById(requireNotNull(extraArguments), requireNotNull(singleValues)).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "upsert entity in table $tableNameExpr by id $idExpr"
}
