package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Select One Entity")
@Description("Selects at most one row and stores it by column name, such as {_user::name}. A nested where block may filter it. Selects always wait, whether or not and wait is written, and failures are exposed as the last database error.")
@Example(
    """
select one entity from table "users" and store the result in {_user::*}:
    where any:
        name = "Alice"
        age > 25
send "name: %{_user::name}%, age: %{_user::age}%"
"""
)
@Since("1.0.0")
class SecSelectOne : SecSelectBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecSelectOne::class.java,
                "select one [entity] from [table] %string% [and] store [the] [result] in %objects% [and wait]"
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
        queries.selectOne(whereClause).execute(table).cursor.use { cursor ->
            if (cursor.next()) {
                table.columns.values.forEach { column ->
                    result[column.name] = cursor.get(column.name, column.type)
                }
            }
        }
        return result
    }

    override fun toString(event: Event?, debug: Boolean): String = buildString {
        append("select one from table $tableNameExpr")
        append(" and store the result in $resultVar")
        if (where != null) append(" where...")
    }
}
