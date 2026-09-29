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

/**
 * `execute command`, the raw statement a document backend takes.
 *
 * It is a separate statement from `execute query` rather than a spelling of it, because a command document
 * and a SQL statement are different things: one is answered with a document and reports what it changed
 * inside that answer, the other returns rows or a count. Keeping them apart is also what lets each be
 * refused by the connection that cannot take it, with the alternative named.
 *
 * It is written without a colon because it has no body to give. See the Raw statements page before using it.
 */
@Name("Execute Raw Command")
@Description("Sends a command document of your own to the connected document database, written as JSON text, and stores the document the server answers with. UNSAFE: the command is sent as written — it is not checked against any registered table, and the plugin does not interpret it. There are no parameters: the command is the whole statement. It fails when the connection takes SQL statements instead of commands.")
@Example(
    """execute command "{ ""count"": ""users"" }" and store the result in {_answer::*}
send "There are %{_answer::n}% users."
"""
)
@Since("1.3.0")
class EffExecuteCommand : EffRawStatementBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffExecuteCommand::class.java,
                "execute command %string% and store [the] [result] in %objects% [and wait]"
            )
        }
    }

    private lateinit var resultVar: Variable<*>

    /** A command document is the whole statement, so this pattern has no parameter slot. */
    override fun parameterIndex(matchedPattern: Int): Int = -1

    /** This pattern ends with the result variable, so the last slot is not a count target. */
    override val storesAffectedRows: Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        val result = expressions[1]
        if (result !is Variable<*> || !result.isList) {
            Skript.error("A raw command's answer must be stored in a list variable, for example {_answer::*}.")
            return false
        }
        resultVar = result
        return super.init(expressions, matchedPattern, isDelayed, parseResult)
    }

    override suspend fun executeStatement(
        queries: Queries,
        statement: String,
        parameters: List<Any?>
    ): Any? = queries.rawCommand(statement).execute()

    @Suppress("UNCHECKED_CAST")
    override fun deliver(event: Event, result: Any?) {
        VariableModifier.writeMap(resultVar, event, result as Map<String, Any?>)
    }

    override fun toString(event: Event?, debug: Boolean): String =
        "execute command $statementExpr and store the result in $resultVar"
}
