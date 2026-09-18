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

@Name("Delete Entity By ID")
@Description("Deletes one row by its registered primary-key value. With and wait, failures are available as the last database error; otherwise execution continues immediately and asynchronous failures are only logged. The store affected rows clause keeps the number of rows the statement affected.")
@Example(
    """delete one entity from table "users" by id {_id} and wait
if last database error is set:
    send "Delete failed: %last database error%"
"""
)
@Since("1.0.0")
class SecDeleteById : SecWriteBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecDeleteById::class.java,
                "delete [one] [entity] from [table] %string% by id %object% [and store affected rows in %-number%] [wait:and wait]"
            )
        }
    }

    private lateinit var idExpr: Expression<Any>
    override val requiresValues = false

    override fun extractExtraParams(expressions: Array<out Expression<*>?>, matchedPattern: Int) {
        // See SecUpdateById: a `by id 1` literal has to be given a type before it can be read.
        idExpr = ExpressionsHelper.withAnyType(expressions[extraParamsIndex(matchedPattern)]!!)
    }

    override fun resolveExtraArguments(event: Event?): Any =
        requireNotNull(idExpr.getSingle(event)) { "ID expression in 'delete by id' is null." }

    override suspend fun executeWrite(queries: Queries, table: Table, singleValues: Map<String, Any?>?, multipleValues: List<Map<String, Any?>>?, whereClause: WhereClause?, extraArguments: Any?): WriteResult =
        queries.deleteById(requireNotNull(extraArguments)).execute(table)

    override fun toString(event: Event?, debug: Boolean) = "delete entity from table $tableNameExpr by id $idExpr"
}
