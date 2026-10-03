package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.skript.utils.ConnectionScope
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Use Database Connection")
@Description("Uses a named connection for the rest of the current event. The change takes effect immediately. An unknown or disconnected name leaves the current connection unchanged and sets last database error. An in connection section still uses its own connection.")
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
        if (this.trigger == null) return

        val name = nameExpr.getSingle(actualEvent)
        if (name == null) {
            report(actualEvent, "Connection name is null.")
            return
        }

        val connection = Database.connection(name)
        if (connection == null) {
            report(actualEvent, ConnectionScope.unknownConnectionMessage(name))
            return
        }
        if (!connection.isConnected) {
            report(actualEvent, "Connection '$name' is not connected.")
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
                "A database transaction is open on another connection. Roll it back before switching."
            )
            return
        }

        // The frame carries no owner, so nothing pops it: the switch is meant to outlive this
        // statement and last until the event does.
        ConnectionScope.push(actualEvent, connection, owner = null, transaction = open)
        DatabaseWork.clearErrorForStatement(actualEvent)
    }

    private fun report(event: Event, message: String) {
        SkriptDatabaseErrors.set(event, message)
        this.error(message)
    }

    override fun toString(event: Event?, debug: Boolean) =
        "use connection ${nameExpr.toString(event, debug)}"
}
