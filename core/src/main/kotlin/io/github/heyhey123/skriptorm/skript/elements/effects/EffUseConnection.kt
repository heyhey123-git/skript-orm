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
 * Switches the connection the rest of the event runs against.
 *
 * This is the effect form of `in connection`, and the difference between them is how long the switch
 * lasts. This one lasts until the event is over, so a command that names its database once at the top
 * does not have to repeat it; the section lasts for its own body, so it is what a script uses when it
 * reads from one database and writes to another.
 *
 * It does no database work, so unlike every other statement in this addon it does not wait: the very
 * next statement already runs against the connection it named.
 *
 * A name that is unknown, or that belongs to a connection which has been disconnected, changes
 * nothing and reports through `last database error`. Keeping the previous connection is deliberate:
 * the alternative would be running the rest of the script against a database it did not choose.
 */
@Name("Use Database Connection")
@Description("Makes a named connection the one the rest of the current event uses. Takes effect immediately, without waiting. An unknown or disconnected name changes nothing and is reported as the last database error. Statements inside an 'in connection' section keep using that section's connection.")
@Example(
    """on load:
    create a connection named "logs" to database "MySQL" with properties:
        url: "jdbc:mysql://localhost:3306/logs"
        username: "root"
        password: "123456"

command /newlog:
    trigger:
        use connection "logs"
        insert one entity into table "entries":
            values:
                message: "the command ran"
"""
)
@Since("1.0.0")
class EffUseConnection : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffUseConnection::class.java,
                "use [the] [database] connection %string%"
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

        val connection = Database.connection(name)
        if (connection == null) {
            report(actualEvent, trigger, ConnectionScope.unknownConnectionMessage(name))
            return
        }
        if (!connection.isConnected) {
            report(actualEvent, trigger, "Connection '$name' is not connected.")
            return
        }

        // Switching inside a transaction would send the statements after this one to a connection the
        // transaction knows nothing about, which is a silent way to lose the atomicity the script asked
        // for. Naming the connection the transaction already uses stays allowed, and the frame it pushes
        // carries the transaction for that reason: without it the switch would look like a no-op while
        // every later statement ran outside the transaction, and the transaction itself would be left
        // for the watchdog to end.
        val open = ConnectionScope.transaction(actualEvent)
        if (open != null && open.database !== connection) {
            report(
                actualEvent,
                trigger,
                "A database transaction is open on another connection. Roll it back before switching."
            )
            return
        }

        // The frame carries no owner, so nothing pops it: the switch is meant to outlive this
        // statement and last until the event does.
        ConnectionScope.push(actualEvent, connection, owner = null, transaction = open)
        DatabaseWork.clearErrorForStatement(actualEvent)
    }

    private fun report(event: Event, trigger: Trigger, message: String) {
        SkriptDatabaseErrors.set(event, message)
        ErrorPrinter.printErrorMessageWithDetail(trigger, message)
    }

    override fun toString(event: Event?, debug: Boolean) =
        "use connection ${nameExpr.toString(event, debug)}"
}
