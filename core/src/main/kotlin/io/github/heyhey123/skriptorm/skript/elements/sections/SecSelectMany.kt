package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Select Many Entities")
@Description("Selects matching rows using row-index and column-name keys such as {_users::1::name}. The one-based row index remains even for one result. Selects always wait and expose failures as the last database error.")
@Example(
    """select many entities from table "users" and store the results in {_users::*}:
    where all:
        active = true
send "%{_users::1::name}%"
"""
)
@Since("1.0.0")
class SecSelectMany : SecSelectBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecSelectMany::class.java,
                "select many [entities] from [table] %string% [and] store [the] [results] in %objects% [and wait]"
            )
        }
    }

    override val tableNameIndex: Int = 0

    override val resultVarIndex: Int = 1

    override suspend fun executeQuery(
        queries: Queries,
        table: Table,
        whereClause: WhereClause?,
        extraArguments: Any?
    ): Map<String, Any?> {
        val result = linkedMapOf<String, Any?>()
        queries.selectMany(whereClause).execute(table).cursor.use { cursor ->
            var rowIndex = 1
            while (cursor.next()) {
                table.columns.values.forEach { column ->
                    result["$rowIndex::${column.name}"] = cursor.get(column.name, column.type)
                }
                rowIndex++
            }
        }
        return result
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "select many from table $tableNameExpr and store in $resultVar${if (where != null) " where..." else ""}"
}
