package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Variable
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.skript.utils.VariableModifier
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Execute Raw Query")
@Description("Sends a SQL statement of your own to the connected database and stores the rows it returns, keyed by the column label and numbered from one, such as {_rows::1::name}. Values are bound to ? placeholders by the with clause. UNSAFE: the statement is sent as written — it is not checked against any registered table, and the plugin does not translate it for the implementation. It does not accept where, and it fails when the connection takes commands instead of SQL.")
@Example(
    """execute query "SELECT id, name FROM users WHERE age > ?" with (18) and store the result in {_rows::*}
loop {_rows::*}:
    send "%{_rows::%loop-index%::name}%"
"""
)
@Since("1.3.0")
class EffExecuteQuery : EffRawStatementBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffExecuteQuery::class.java,
                "execute query %string% [with %-objects%] and store [the] [result] in %objects% [and wait]"
            )
        }
    }

    private lateinit var resultVar: Variable<*>

    override fun parameterIndex(matchedPattern: Int): Int = 1

    /** This pattern ends with the result variable, so the last slot is not a count target. */
    override val storesAffectedRows: Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        val result = expressions[2]
        if (result !is Variable<*> || !result.isList) {
            Skript.error("Raw query results must be stored in a list variable, for example {_rows::*}.")
            return false
        }
        resultVar = result
        return super.init(expressions, matchedPattern, isDelayed, parseResult)
    }

    override suspend fun executeStatement(
        queries: Queries,
        statement: String,
        parameters: List<Any?>
    ): Any? = queries.rawQuery(statement, parameters).execute()

    @Suppress("UNCHECKED_CAST")
    override fun deliver(event: Event, result: Any?) {
        VariableModifier.writeMap(resultVar, event, result as Map<String, Any?>)
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "execute query $statementExpr and store the result in $resultVar"
}
