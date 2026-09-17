package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Trigger
import ch.njol.skript.lang.TriggerItem
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.skript.utils.ConnectionScope
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.ErrorPrinter
import io.github.heyhey123.skriptorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * Runs its body against a named connection.
 *
 * This is the scoped half of connection switching, and the one a script reaches for when it reads from
 * one database and writes to another. Unlike `use connection`, which lasts until the event ends, this
 * lasts exactly as long as the body, and a `use connection` written inside it is taken back when the
 * body is left.
 *
 * A transaction is a connection that was pinned, so this section refuses to switch away from one: a
 * statement that quietly ran outside the transaction would be worse than a report saying it cannot.
 *
 * A name that is unknown, or that belongs to a disconnected connection, skips the body and reports
 * through `last database error`. Running the body against the default connection instead would be a
 * silent wrong-database write, which is the one outcome worth failing for.
 */
@Name("In Database Connection")
@Description("Runs the code inside against a named connection. The switch lasts only for the body; a 'use connection' written inside it is undone when the body ends. An unknown or disconnected name skips the body and is reported as the last database error. Switching away from the connection an open transaction is using is refused.")
@Example(
    """
in connection "logs":
    insert one entity into table "entries":
        values:
            message: "written to the logs database"
"""
)
@Since("1.0")
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
        val trigger = this.trigger ?: return walk(actualEvent, false)

        val name = nameExpr.getSingle(actualEvent)
        if (name == null) {
            report(actualEvent, trigger, "Connection name is null.")
            return walk(actualEvent, false)
        }

        val connection = Database.connection(name)
        if (connection == null) {
            report(actualEvent, trigger, ConnectionScope.unknownConnectionMessage(name))
            return walk(actualEvent, false)
        }
        if (!connection.isConnected) {
            report(actualEvent, trigger, "Connection '$name' is not connected.")
            return walk(actualEvent, false)
        }

        val transaction = ConnectionScope.transaction(actualEvent)
        if (transaction != null && transaction.database !== connection) {
            report(
                actualEvent,
                trigger,
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
        ConnectionScope.push(actualEvent, connection, this)
        return walk(actualEvent, true)
    }

    override fun onBodyEnd(event: Event, continuation: TriggerItem?): TriggerItem? {
        ConnectionScope.popOwned(event, this)
        return continuation
    }

    override fun leaveBody(event: Event) {
        ConnectionScope.popOwned(event, this)
    }

    private fun report(event: Event, trigger: Trigger, message: String) {
        SkriptDatabaseErrors.set(event, message)
        ErrorPrinter.printErrorMessageWithDetail(trigger, message)
    }

    override fun toString(event: Event?, debug: Boolean) =
        "in connection ${nameExpr.toString(event, debug)}"
}
