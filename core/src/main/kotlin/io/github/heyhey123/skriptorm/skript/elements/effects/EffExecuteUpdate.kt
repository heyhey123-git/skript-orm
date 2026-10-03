package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.doc.*
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.skript.utils.AffectedRows
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Execute Raw Update")
@Description("Runs a SQL statement such as ALTER TABLE or UPDATE. Use with to bind values to ? placeholders. You can store the affected-row count reported by the database; statements without a count report 0. The SQL is sent as written, without checking registered tables or translating it for the database. It does not work with command-based databases. Check last database error for failures.")
@Example(
    """execute update "ALTER TABLE users ADD COLUMN age INT NULL"
set {_values::*} to 30, "Alice"
execute update "UPDATE users SET age = ? WHERE name = ?" with {_values::*} and store affected rows in {_rows}
"""
)
@Since("1.3.0")
class EffExecuteUpdate : EffRawStatementBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffExecuteUpdate::class.java,
                "execute update %string% [with %-objects%] [and store affected rows in %-number%] [and wait]"
            )
        }
    }

    override fun parameterIndex(matchedPattern: Int): Int = 1

    override suspend fun executeStatement(
        queries: Queries,
        statement: String,
        parameters: List<Any?>
    ): Any? = queries.rawUpdate(statement, parameters).execute()

    override fun deliver(event: Event, result: Any?) {
        AffectedRows.write(affectedRowsVariable, event, result as WriteResult)
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "execute update $statementExpr"
}
