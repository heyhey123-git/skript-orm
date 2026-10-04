package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.skript.utils.ExpressionsHelper
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Select Entity By ID")
@Description("Selects the row with the given primary-key value. Columns are stored by name, such as {_user::name}. This form does not accept a where block. The next line runs after the query finishes. Check last database error for failures.")
@Example(
    """select entity from table "users" by id {_id} and store the result in {_user::*}
send "%{_user::name}%"
"""
)
@Since("1.0.0")
class SecSelectById : SecSelectBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecSelectById::class.java,
                "select [one] [entity] from [table] %string% by id %object% [and] store [the] [result] in %objects% [and wait]"
            )
        }
    }

    private lateinit var idExpr: Expression<Any>

    override val tableNameIndex = 0
    override val resultVarIndex = 2
    override val supportsWhere = false

    override fun extractExtraParams(expressions: Array<out Expression<*>?>) {
        // A `by id 1` literal is handed over untyped, so it is given a type here rather than read
        // straight from the pattern; see SecUpdateById for what happens otherwise.
        idExpr = ExpressionsHelper.withAnyType(expressions[1]!!)
    }

    override fun resolveExtraArguments(event: Event?): Any =
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
                    result[column.name] = cursor.getDetached(column.name, column.type)
                }
            }
        }
        return result
    }

    override fun toString(event: Event?, debug: Boolean) =
        "select from table $tableNameExpr by id $idExpr and store the result in $resultVar"
}
