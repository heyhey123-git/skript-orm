package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.skript.utils.ExpressionsHelper
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Update Entity By ID")
@Description("Updates one row by its registered primary-key value. The new values may be written in the section body, or taken from a list variable shaped like a select result. The statement waits: the lines after it run once the database has taken the change, and a failure is available as the last database error. The store affected rows clause keeps the number of rows the statement affected.")
@Example(
    """update one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
"""
)
@Example(
    """update one entity {_changes::*} in table "users" by id {_id}
"""
)
@Since("1.0.0")
class SecUpdateById : SecWriteBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecUpdateById::class.java,
                "update [one] [entity] in [table] %string% by id %object% [and store affected rows in %-number%] [wait:and wait]",
                "update [one] [entity] %objects% in [table] %string% by id %object% [and store affected rows in %-number%] [wait:and wait]"
            )
        }
    }

    private lateinit var idExpr: Expression<Any>

    override fun valuesExpressionIndex(matchedPattern: Int) = if (matchedPattern == 0) -1 else 0

    override fun extractExtraParams(expressions: Array<out Expression<*>?>, matchedPattern: Int) {
        // `by id 1` reaches this section as a literal Skript has not given a type to, because a
        // `%object%` slot leaves that decision to the syntax that reads it; passing it on unchanged
        // makes the read throw at runtime instead of returning the number.
        idExpr = ExpressionsHelper.withAnyType(expressions[extraParamsIndex(matchedPattern)]!!)
    }

    override fun resolveExtraArguments(event: Event?): Any =
        requireNotNull(idExpr.getSingle(event)) { "ID expression in 'update by id' is null." }

    override suspend fun executeWrite(queries: Queries, table: Table, singleValues: Map<String, Any?>?, multipleValues: List<Map<String, Any?>>?, whereClause: WhereClause?, extraArguments: Any?): WriteResult {
        // Patch semantics, as in SecUpdate: only the supplied columns form the SET list, and an
        // omitted column is left untouched rather than written as NULL.
        return queries.updateById(requireNotNull(extraArguments), requireNotNull(singleValues)).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "update entity in table $tableNameExpr by id $idExpr"
}
