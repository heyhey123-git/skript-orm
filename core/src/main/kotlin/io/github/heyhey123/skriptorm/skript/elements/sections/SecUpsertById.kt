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

@Name("Upsert Entity By ID")
@Description("Updates the row with the given primary-key value or inserts it when absent. The values may be written in the section body, or taken from a list variable shaped like a select result. Support depends on the implementation. The statement waits: the lines after it run once the database has taken the change, and a failure is available as the last database error. The store affected rows clause keeps the number of rows the statement affected.")
@Example(
    """upsert one entity in table "users" by id {_id} and wait:
    values:
        name: "Alice"
        age: 26
"""
)
@Example(
    """upsert one entity {_user::*} in table "users" by id {_id}
"""
)
@Since("1.0.0")
class SecUpsertById : SecWriteBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecUpsertById::class.java,
                "upsert [one] [entity] in [table] %string% by id %object% [and store affected rows in %-number%] [wait:and wait]",
                "upsert [one] [entity] %objects% in [table] %string% by id %object% [and store affected rows in %-number%] [wait:and wait]"
            )
        }
    }

    private lateinit var idExpr: Expression<Any>

    override fun valuesExpressionIndex(matchedPattern: Int) = if (matchedPattern == 0) -1 else 0

    override fun extractExtraParams(expressions: Array<out Expression<*>?>, matchedPattern: Int) {
        // See SecUpdateById: a `by id 1` literal has to be given a type before it can be read.
        idExpr = ExpressionsHelper.withAnyType(expressions[extraParamsIndex(matchedPattern)]!!)
    }

    override fun resolveExtraArguments(event: Event?): Any =
        requireNotNull(idExpr.getSingle(event)) { "ID expression in 'upsert by id' is null." }

    override suspend fun executeWrite(queries: Queries, table: Table, singleValues: Map<String, Any?>?, multipleValues: List<Map<String, Any?>>?, whereClause: WhereClause?, extraArguments: Any?): WriteResult {
        // The supplied columns feed both branches of the statement: an absent row is inserted with
        // only them, so its other columns fall back to the database default, and an existing row is
        // patched with only them, so its other columns keep their stored value. A column the values
        // omit therefore never becomes NULL in either branch.
        return queries.upsertById(requireNotNull(extraArguments), requireNotNull(singleValues)).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "upsert entity in table $tableNameExpr by id $idExpr"
}
