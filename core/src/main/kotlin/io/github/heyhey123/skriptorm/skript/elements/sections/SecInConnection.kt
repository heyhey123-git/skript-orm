package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.skript.utils.ConnectionScope
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("In Database Connection")
@Description("Runs the section using a named connection, then restores the previous one. A use connection effect inside the section also ends when the section ends. If the name is unknown or disconnected, the section is skipped and last database error is set. An open transaction cannot switch to another connection.")
@Example(
    """in connection "logs":
    insert one entity into table "entries":
        values:
            message: "written to the logs database"
"""
)
@Since("1.0.0")
class SecInConnection : ScopedBodySection() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecInConnection::class.java,
                "in [the] [database] connection %string%"
            )
        }
    }

    private lateinit var nameExpr: Expression<String>

    @Suppress("UNCHECKED_CAST")
    override fun prepare(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        nameExpr = expressions[0] as Expression<String>
        return true
    }

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return walk(event, false)
        if (this.trigger == null) return walk(actualEvent, false)

        val name = nameExpr.getSingle(actualEvent)
        if (name == null) {
            report(actualEvent, "Connection name is null.")
            return walk(actualEvent, false)
        }

        val connection = Database.connection(name)
        if (connection == null) {
            report(actualEvent, ConnectionScope.unknownConnectionMessage(name))
            return walk(actualEvent, false)
        }
        if (!connection.isConnected) {
            report(actualEvent, "Connection '$name' is not connected.")
            return walk(actualEvent, false)
        }

        val transaction = ConnectionScope.transaction(actualEvent)
        if (transaction != null && transaction.database !== connection) {
            report(
                actualEvent,
                "A database transaction is open on another connection. Roll it back before switching."
            )
            return walk(actualEvent, false)
        }

        if (first == null) {
            // Nothing to scope. Pushing a frame here would leave one that no body reaches the end of,
            // so it would outlive the section and silently capture the rest of the event.
            return walk(actualEvent, false)
        }

        DatabaseWork.clearErrorForStatement(actualEvent)
        // Naming the connection a transaction is already running on is allowed, and the frame carries
        // that transaction: the block runs on the same connection, so the statements inside it belong to
        // the transaction like the ones around it. A frame without it would send them through the pool,
        // where they would commit on their own and survive a rollback.
        ConnectionScope.push(actualEvent, connection, this, transaction = transaction)
        return walk(actualEvent, true)
    }

    override fun onBodyEnd(event: Event, continuation: TriggerItem?): TriggerItem? {
        ConnectionScope.popOwned(event, this)
        return continuation
    }

    override fun leaveBody(event: Event) {
        ConnectionScope.popOwned(event, this)
    }

    private fun report(event: Event, message: String) {
        SkriptDatabaseErrors.set(event, message)
        this.error(message)
    }

    override fun toString(event: Event?, debug: Boolean) =
        "in connection ${nameExpr.toString(event, debug)}"
}
