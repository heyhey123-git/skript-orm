package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Trigger
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.database.Transaction
import io.github.heyhey123.skriptorm.skript.utils.ConnectionScope
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.ErrorPrinter
import io.github.heyhey123.skriptorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * Closes a connection, waits for it, and lets the trigger carry on.
 *
 * Three forms share this element because they differ only in which connection they choose: the one in
 * effect, one named by the script, or all of them.
 *
 * The unqualified form resolves the way every other statement does, so inside `in connection "logs":`
 * it closes `"logs"`, after `use connection "logs"` it closes `"logs"`, and with neither it closes the
 * default. It used to always close the default, which made `[current]`, the one word of its pattern
 * that promises otherwise, a lie, and had a destructive statement act on a connection the script was
 * not using.
 *
 * The connection is resolved before the work is handed off, on the server thread, because that is where
 * the scopes live. It cannot be kept in a field either: Skript reuses one element instance for every
 * event that reaches it, so two triggers would overwrite each other's target. The closure
 * [DatabaseWork.run] takes is what carries it.
 */
@Name("Disconnect Database")
@Description("Disconnects a database connection asynchronously. Without a name it closes the connection in effect: the innermost 'in connection' scope, then a 'use connection' from this event, then the default one. The named form closes one connection, and the all form closes every connection. The following trigger item runs after disconnection finishes. Failures are logged and exposed as the last database error.")
@Example("disconnect from the current database")
@Example("disconnect from connection \"logs\"")
@Example("disconnect from all connections")
@Since("1.0.0")
class EffDisconnect : Effect() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.effect(
                addon,
                EffDisconnect::class.java,
                // The named form is registered before the default one: both could otherwise be tried
                // against `disconnect from the connection "x"`, and the literal in this one settles it.
                "disconnect [from] all [database] connections",
                "disconnect [from] [the] [database] connection %string%",
                "disconnect [from] [the] [current] [database] [connection]"
            )
        }
    }

    private var matchedPattern: Int = 2
    private var nameExpr: Expression<String>? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        parser.hasDelayBefore = Kleenean.TRUE
        this.matchedPattern = matchedPattern
        if (matchedPattern == 1) {
            nameExpr = expressions[0] as Expression<String>
        }
        return true
    }

    /** Never called: the work is in [walk], the only place that runs on the server thread. */
    override fun execute(event: Event?) = Unit

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return next
        val trigger = this.trigger ?: return next

        // Closing a connection rolls back whatever transaction is open on it, and doing that behind the
        // script's back would turn a half-finished transaction into a silent rollback. Lifecycle paths
        // (shutdown, replacing a connection) still abort transactions, because there the connection is
        // going away whether the script is ready or not.
        val open = ConnectionScope.transaction(actualEvent)
        if (open != null && targets(actualEvent, open)) {
            report(
                actualEvent,
                trigger,
                "A database transaction is open on this connection. Roll it back before disconnecting it."
            )
            return next
        }

        val disconnect = when (matchedPattern) {
            0 -> everyConnection()
            1 -> namedConnection(actualEvent, trigger)
            else -> connectionInEffect(actualEvent)
        } ?: return next

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            wait = true,
            query = { disconnect() },
            onFailure = { error ->
                ErrorPrinter.printErrorMessageWithDetail(trigger, "Disconnect failed: ${error.message}")
            }
        )
    }

    /** Whether the form the script wrote would close the connection [open] is running on. */
    private fun targets(event: Event, open: Transaction): Boolean = when (matchedPattern) {
        0 -> true
        1 -> nameExpr?.getSingle(event)?.let { Database.connection(it) } === open.database
        else -> ConnectionScope.resolve(event) === open.database
    }

    /** Every connection, or nothing to do when none was ever created. */
    private fun everyConnection(): (suspend () -> Unit)? {
        if (Database.current == null && Database.connectionNames.isEmpty()) return null
        return { Database.disconnectAll() }
    }

    private fun namedConnection(event: Event, trigger: Trigger): (suspend () -> Unit)? {
        val name = nameExpr?.getSingle(event)
        if (name == null) {
            report(event, trigger, "Connection name is null.")
            return null
        }

        val connection = Database.connection(name)
        if (connection == null) {
            report(event, trigger, ConnectionScope.unknownConnectionMessage(name))
            return null
        }

        return { connection.disconnect() }
    }

    /**
     * The connection in effect, or nothing to do when none is.
     *
     * One that is in effect but already disconnected is closed rather than reported: that is a no-op,
     * and reporting it would tell the script something went wrong when nothing did.
     */
    private fun connectionInEffect(event: Event): (suspend () -> Unit)? {
        val connection = ConnectionScope.resolve(event) ?: return null
        return { connection.disconnect() }
    }

    private fun report(event: Event, trigger: Trigger, message: String) {
        SkriptDatabaseErrors.set(event, message)
        ErrorPrinter.printErrorMessageWithDetail(trigger, message)
    }

    override fun toString(event: Event?, debug: Boolean) = when (matchedPattern) {
        0 -> "disconnect from all connections"
        1 -> "disconnect from connection ${nameExpr?.toString(event, debug)}"
        else -> "disconnect from the current database connection"
    }
}
