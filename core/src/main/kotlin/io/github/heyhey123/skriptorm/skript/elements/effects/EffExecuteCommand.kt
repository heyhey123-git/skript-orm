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

@Name("Execute Raw Command")
@Description("Runs a JSON command on a document database and stores the response document. The command is sent as written, without checking registered tables or binding parameters. It does not work with SQL connections.")
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

    override val resultVariable: Variable<*> get() = resultVar

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
