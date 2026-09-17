package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.config.SectionNode
import ch.njol.skript.doc.*
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import ch.njol.skript.lang.SectionExitHandler
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Trigger
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.skript.utils.ConnectionScope
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptSyntax
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

/**
 * Runs its body against a named connection.
 *
 * This is the scoped half of connection switching, and the one a script reaches for when it reads
 * from one database and writes to another. Unlike `use connection`, which lasts until the event ends,
 * this lasts exactly as long as the body, and a `use connection` written inside it is taken back when
 * the body is left.
 *
 * The body is code, so it is loaded with `loadCode`, which is also what puts this section into the
 * parser's current sections: that is how Skript can tell us the body is being left by an `exit`,
 * `stop` or `return` rather than by reaching its end. Both paths pop the frame, and the frame is
 * popped before the statement after the section runs either way.
 *
 * A name that is unknown, or that belongs to a disconnected connection, skips the body and reports
 * through `last database error`. Running the body against the default connection instead would be a
 * silent wrong-database write, which is the one outcome worth failing for.
 */
@Name("In Database Connection")
@Description("Runs the code inside against a named connection. The switch lasts only for the body; a 'use connection' written inside it is undone when the body ends. An unknown or disconnected name skips the body and is reported as the last database error.")
@Example(
    """
in connection "logs":
    insert one entity into table "entries":
        values:
            message: "written to the logs database"
"""
)
@Since("1.0")
class SecInConnection : Section(), SectionExitHandler {

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
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult,
        sectionNode: SectionNode,
        triggerItems: List<TriggerItem?>
    ): Boolean {
        nameExpr = expressions[0] as Expression<String>
        loadCode(sectionNode)
        splicePopNode()
        return true
    }

    /**
     * Puts the pop node after the body's last statement, and moves [last] onto it.
     *
     * A section cannot observe its own body ending: the loader points the body's last item at
     * whatever follows the section, and it links the section itself through `setNext`, which
     * `TriggerSection` forwards to [last]. Doing this at parse time covers both cases. When the
     * section is followed by another statement, the loader's `setNext` reaches the pop node through
     * [last] and keeps the chain in order; when the section is the last statement of a trigger, that
     * call never happens at all, and the node spliced here is the only thing that pops the frame.
     */
    private fun splicePopNode() {
        val bodyLast = last
        val node = PopConnectionScope(this)
        if (bodyLast != null) {
            bodyLast.setNext(node)
            last = node
        }
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

        if (first == null) {
            // Nothing to scope. Pushing a frame here would leave one that no body reaches the end of,
            // so it would outlive the section and silently capture the rest of the event.
            return walk(actualEvent, false)
        }

        SkriptDatabaseErrors.clear(actualEvent)
        ConnectionScope.push(actualEvent, connection, this)
        return walk(actualEvent, true)
    }

    /**
     * Called when `exit`, `stop` or `return` leaves the body before its end.
     *
     * The pop node is skipped by those jumps, so this is the only chance to drop the frame. Failing
     * to do it would leave the rest of the event resolving to this section's connection.
     */
    override fun exit(event: Event) {
        ConnectionScope.popOwned(event, this)
    }

    private fun report(event: Event, trigger: Trigger, message: String) {
        SkriptDatabaseErrors.set(event, message)
        ErrorPrinter.printErrorMessageWithDetail(trigger, message)
    }

    override fun toString(event: Event?, debug: Boolean) =
        "in connection ${nameExpr.toString(event, debug)}"
}

/**
 * The statement that ends a [SecInConnection] body.
 *
 * A section cannot observe its own body ending, because the loader points the body's last item at
 * whatever follows the section. This node is placed in between so that the end of the body is a
 * statement like any other, and it works across a `wait` for the same reason: the continuation
 * resumes inside the body and walks on to it.
 */
private class PopConnectionScope(private val section: SecInConnection) : TriggerItem(section) {

    /** Never called: this item overrides [walk], which is what the loader walks. */
    override fun run(event: Event): Boolean = throw UnsupportedOperationException()

    override fun walk(event: Event?): TriggerItem? {
        if (event != null) {
            debug(event, true)
            ConnectionScope.popOwned(event, section)
        }
        return getNext()
    }

    override fun toString(event: Event?, debug: Boolean) = "end of in connection"
}
