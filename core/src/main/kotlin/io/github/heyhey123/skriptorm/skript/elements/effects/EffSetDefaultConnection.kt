package io.github.heyhey123.skriptorm.skript.elements.effects

import ch.njol.skript.doc.*
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.skript.utils.ConnectionScope
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
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
 * The connection that loses the role is closed when it has no name to be reached by: no statement can
 * resolve to it afterwards, so holding its pool open would only be a leak. That close is part of the
 * statement, which is why this one waits where `use connection` does not.
 */
@Name("Make Database Connection The Default")
@Description("Makes a named connection the one statements use when neither a scope nor a 'use connection' effect names one. The first connection created is already the default, so this is only needed to choose a different one. The connection that loses the role is disconnected when it has no name of its own, because nothing can reach it afterwards; a named one keeps running. An unknown or disconnected name changes nothing and is reported as the last database error, and the statement is refused while a database transaction is open.")
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
        // The connection that loses the role may be closed, and a script waits for that: the lines after
        // this one see the new default.
        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    /** Never called: the work is in [walk], the only place that runs on the server thread. */
    override fun execute(event: Event?) = Unit

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return next
        if (this.trigger == null) return next

        // Every refusal here goes through DatabaseWork.report rather than through a message of its own,
        // so a statement that did not run counts the same as one that failed: inside a transaction, the
        // body is undone rather than committed as if nothing had happened.
        val name = nameExpr.getSingle(actualEvent)
        if (name == null) {
            DatabaseWork.report(actualEvent, this, "Connection name is null.")
            return next
        }

        // Resolved by hand rather than through `makeDefault`, so an unknown name and a disconnected
        // one can say which of the two happened.
        val connection = Database.connection(name)
        if (connection == null) {
            DatabaseWork.report(actualEvent, this, ConnectionScope.unknownConnectionMessage(name))
            return next
        }
        if (!connection.isConnected) {
            DatabaseWork.report(actualEvent, this, "Connection '$name' is not connected.")
            return next
        }

        // The connection that loses the role may be the one a transaction is running on, and closing it
        // would take that transaction with it. `create a connection` refuses inside one for the same
        // reason.
        if (ConnectionScope.transaction(actualEvent) != null) {
            DatabaseWork.report(
                actualEvent,
                this,
                "The default connection cannot be changed inside a database transaction. Roll it back first."
            )
            return next
        }

        return DatabaseWork.run(
            event = actualEvent,
            continuation = next,
            query = { Database.makeDefault(name) },
            deliver = { moved ->
                // The name was there a moment ago, so this is a connection that went away while the
                // statement was waiting for its turn.
                if (!moved) {
                    DatabaseWork.report(actualEvent, this, ConnectionScope.unknownConnectionMessage(name))
                }
            }
        )
    }

    override fun toString(event: Event?, debug: Boolean) =
        "make connection ${nameExpr.toString(event, debug)} the default"
}
