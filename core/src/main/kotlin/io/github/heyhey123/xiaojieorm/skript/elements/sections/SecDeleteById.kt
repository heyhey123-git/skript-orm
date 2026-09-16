package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptSyntax
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Delete Entity By ID")
@Description("Deletes one row by its registered primary-key value. With and wait, failures are available as the last database error; otherwise execution continues immediately and asynchronous failures are only logged.")
@Example(
    """
delete one entity from table "users" by id {_id} and wait:
if last database error is set:
    send "Delete failed: %last database error%"
"""
)
@Since("1.0")
class SecDeleteById : SecWriteBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(addon, SecDeleteById::class.java, "delete [one] [entity] from [table] %string% by id %object% [wait:and wait]")
        }
    }

    private lateinit var idExpr: Expression<Any>
    override val requiresValues = false

    @Suppress("UNCHECKED_CAST")
    override fun extractExtraParams(expressions: Array<out Expression<*>?>, matchedPattern: Int) {
        idExpr = expressions[extraParamsIndex(matchedPattern)] as Expression<Any>
    }

    override fun resolveExtraArguments(event: Event?): Any =
        requireNotNull(idExpr.getSingle(event)) { "ID expression in 'delete by id' is null." }

    override suspend fun executeWrite(queries: Queries, table: Table, singleValues: Map<String, Any?>?, multipleValues: List<Map<String, Any?>>?, whereClause: WhereClause?, extraArguments: Any?) {
        queries.deleteById(requireNotNull(extraArguments)).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "delete entity from table $tableNameExpr by id $idExpr"
}
