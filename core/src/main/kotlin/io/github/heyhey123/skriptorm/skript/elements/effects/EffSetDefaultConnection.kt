package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Trigger
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.skript.utils.ConnectionScope
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.ErrorPrinter
import io.github.heyhey123.skriptorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * Chooses which connection statements use when nothing names one.
 *
 * The first connection that succeeds becomes the default on its own, so this is only needed once a
 * script has more than one and wants a different one to answer unqualified statements. It changes
 * what later events resolve to; the event it runs in is unaffected unless it also says
 * `use connection`, because a statement that already resolved its connection keeps it.
 *
 * Like `use connection` it does no database work and does not wait.
 */
@Name("Make Database Connection The Default")
@Description("Makes a named connection the one statements use when neither a scope nor a 'use connection' effect names one. The first connection created is already the default, so this is only needed to choose a different one. An unknown or disconnected name changes nothing and is reported as the last database error.")
@Example(
    """make connection "logs" the default
"""
)
@Since("1.0.0")
class EffSetDefaultConnection : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffSetDefaultConnection::class.java,
                "make [the] connection %string% the default [database] [connection]"
            )
        }
    }

    private lateinit var nameExpr: Expression<String>

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        nameExpr = expressions[0] as Expression<String>
        return true
    }

    override fun execute(event: Event?) {
        val actualEvent = event ?: return
        val trigger = this.trigger ?: return

        val name = nameExpr.getSingle(actualEvent)
        if (name == null) {
            report(actualEvent, trigger, "Connection name is null.")
            return
        }

        // Resolved by hand rather than through `makeDefault`, so an unknown name and a disconnected
        // one can say which of the two happened.
        val connection = Database.connection(name)
        if (connection == null) {
            report(actualEvent, trigger, ConnectionScope.unknownConnectionMessage(name))
            return
        }
        if (!connection.isConnected) {
            report(actualEvent, trigger, "Connection '$name' is not connected.")
            return
        }

        Database.makeDefault(name)
        DatabaseWork.clearErrorForStatement(actualEvent)
    }

    private fun report(event: Event, trigger: Trigger, message: String) {
        SkriptDatabaseErrors.set(event, message)
        ErrorPrinter.printErrorMessageWithDetail(trigger, message)
    }

    override fun toString(event: Event?, debug: Boolean) =
        "make connection ${nameExpr.toString(event, debug)} the default"
}
