package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.skript.utils.SelectResult
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Select Many Entities")
@Description("Selects matching rows into numbered list entries such as {_users::1::name}. Numbering starts at 1, even if only one row matches. Results over 5000 rows are rejected without storing a partial list. The next line runs after the query finishes; check last database error for failures.")
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
    ): Map<String, Any?> = SelectResult.readMany(queries, table, whereClause)

    override fun toString(event: Event?, debug: Boolean): String =
        "select many from table $tableNameExpr and store in $resultVar${if (where != null) " where..." else ""}"
}
