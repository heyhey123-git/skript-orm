package io.github.heyhey123.xiaojieorm.skript.utils

import io.github.heyhey123.xiaojieorm.database.Database
import org.bukkit.event.Event
import java.util.Collections
import java.util.WeakHashMap

/**
 * Which connection a statement runs against, decided while the trigger walks.
 *
 * A statement asks [resolve], which answers in this order:
 *
 * 1. the innermost `in connection` section it is written inside,
 * 2. the connection a `use connection` effect named earlier in the same event,
 * 3. the default connection.
 *
 * The two first answers are frames kept per event. `in connection` pushes one that is popped when its
 * body ends, or when an `exit`/`stop`/`return` leaves it early; `use connection` pushes one that is
 * never popped, because its whole point is to last until the event is over.
 *
 * The frames live on the event rather than on the trigger, so a function called from inside a scope
 * inherits it, and a continuation that resumes after a `wait` still sees it. The cost is that two
 * handlers listening to the same event share the frames, exactly as they already share
 * [SkriptDatabaseErrors]. Both are per event, and both die with it.
 *
 * Only the server thread pushes, pops and reads, because every element resolves its connection before
 * it hands work to another thread. The map is synchronized so that the weak keys themselves stay
 * safe, not because the stack is expected to be touched from two threads at once.
 */
object ConnectionScope {

    /** One decision about which connection is in effect, and what put it there. */
    private class Frame(val connection: Database, val owner: Any?)

    /**
     * Event keys are weak so a finished event is not retained by the scope it happened to use.
     *
     * This matters more than it does for [SkriptDatabaseErrors]: a frame holds a [Database], and a
     * long-lived map entry would keep a closed pool's instance alive for the life of the server.
     */
    private val frames = Collections.synchronizedMap(WeakHashMap<Event, MutableList<Frame>>())

    /**
     * The connection a statement carrying [event] runs against, or null when none is in effect.
     *
     * A frame naming a connection that has since been disconnected is returned as it is, so the
     * statement fails against that connection. Falling back to the default here would send a
     * statement to a database the script did not choose, which is worse than failing.
     */
    fun resolve(event: Event?): Database? {
        val framed = event?.let { current(it)?.connection }
        return framed ?: Database.current
    }

    /** Pushes a connection in effect. [owner] is the section that will pop it, or null for an effect. */
    fun push(event: Event, connection: Database, owner: Any?) {
        val stack = stackOf(event)
        synchronized(stack) {
            stack.add(Frame(connection, owner))
        }
    }

    /**
     * Pops the frame [owner] pushed, together with every frame pushed after it.
     *
     * Truncating rather than removing one frame is what makes `use connection` inside an
     * `in connection` section behave: the effect's frame is above the section's, so leaving the
     * section takes the effect's switch with it instead of leaking it into the rest of the event.
     */
    fun popOwned(event: Event, owner: Any) {
        val stack = frames[event] ?: return
        var emptied = false
        synchronized(stack) {
            val index = stack.indexOfLast { it.owner === owner }
            if (index >= 0) {
                while (stack.size > index) {
                    stack.removeAt(stack.size - 1)
                }
            }
            emptied = stack.isEmpty()
        }
        if (emptied) frames.remove(event, stack)
    }

    /** Forgets every frame of [event], used when nothing about it can be trusted any more. */
    fun clear(event: Event) {
        frames.remove(event)
    }

    /** The message a script sees when it names a connection that has not been created. */
    fun unknownConnectionMessage(name: String): String {
        val known = Database.connectionNames
        return if (known.isEmpty()) {
            "No connection named '$name', and no connection has been created yet."
        } else {
            "No connection named '$name'. Available connections: ${known.joinToString(", ")}."
        }
    }

    /**
     * The message a statement reports when nothing is in effect: no scope, no `use connection`, and no
     * default.
     *
     * A script with a single unnamed connection never sees the second half of this. It only appears
     * once named connections exist without any of them being the default, where "no database
     * connected" would send the reader looking for a connection that is right there.
     */
    fun noConnectionMessage(): String {
        val known = Database.connectionNames
        return if (known.isEmpty()) {
            "No database connected."
        } else {
            "No default database connection. Use 'use connection \"name\"' to choose one; " +
                "available connections: ${known.joinToString(", ")}."
        }
    }

    private fun current(event: Event): Frame? {
        val stack = frames[event] ?: return null
        synchronized(stack) {
            return stack.lastOrNull()
        }
    }

    private fun stackOf(event: Event): MutableList<Frame> =
        frames[event] ?: synchronized(frames) {
            frames.getOrPut(event) { mutableListOf() }
        }
}
