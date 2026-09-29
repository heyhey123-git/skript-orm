package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.doc.*
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.skript.utils.AffectedRows
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * `execute update`, the raw statement that changes the database and reports how many rows it affected.
 *
 * This is also the statement for the things no declaration can express — `ALTER TABLE`, `CREATE INDEX`, a
 * statement only one server understands — which is why the count is optional: a server reports zero for
 * statements it does not count, and a script that does not care should not have to say so.
 *
 * It is written without a colon because it has no body to give, like `delete entities`. See the Raw
 * statements page before using it.
 */
@Name("Execute Raw Update")
@Description("Sends a SQL statement of your own to the connected database, such as ALTER TABLE or UPDATE. Values are bound to ? placeholders by the with clause. The number of affected rows may be stored, which is what the server reports and is 0 for statements it does not count. The statement waits and exposes failures as the last database error. UNSAFE: the statement is sent as written — it is not checked against any registered table, and the plugin does not translate it for the implementation. It fails when the connection takes commands instead of SQL.")
@Example(
    """execute update "ALTER TABLE users ADD COLUMN age INT NULL"
execute update "UPDATE users SET age = ? WHERE name = ?" with (30, "Alice") and store affected rows in {_rows}
"""
)
@Since("1.2.0")
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
