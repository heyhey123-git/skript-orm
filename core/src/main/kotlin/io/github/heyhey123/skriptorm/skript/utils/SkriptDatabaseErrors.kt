package io.github.heyhey123.skriptorm.skript.utils

import org.bukkit.event.Event
import java.util.Collections
import java.util.WeakHashMap

/**
 * Stores the most recent database error for each live Skript event.
 *
 * Event keys are weak so completed events are not retained. Waiting operations can read the stored
 * message through the `last database error` expression after their continuation resumes.
 */
object SkriptDatabaseErrors {

    private val errors = Collections.synchronizedMap(WeakHashMap<Event, String>())

    fun clear(event: Event) {
        errors.remove(event)
    }

    fun set(event: Event, message: String) {
        errors[event] = message
    }

    fun set(event: Event, error: Throwable) {
        set(event, messageOf(error))
    }

    /**
     * The one line a failure is reported with, whether it goes into `last database error` or out through
     * Skript's runtime error channel. An exception without a message is named by its class, so a report
     * never comes out empty.
     *
     * The deepest message in the cause chain is added when it says something the outer one does not.
     * Wrapping is the normal shape of a driver failure — "Failed to connect to the database: <url>"
     * carries a `SQLException` whose own message is the reason — and the outer message alone would leave
     * the script reading a sentence that names the symptom rather than the cause.
     */
    fun messageOf(error: Throwable): String {
        val message = directMessageOf(error)
        val cause = deepestMessageOf(error) ?: return message
        return if (cause == message) message else "$message ($cause)"
    }

    private fun directMessageOf(error: Throwable): String =
        error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName

    /**
     * The innermost non-blank message under [error], or null when there is none besides its own.
     *
     * The chain is walked rather than read once because a driver may wrap its own failure: what a script
     * needs is the sentence the server sent, however many layers the pool put around it.
     */
    private fun deepestMessageOf(error: Throwable): String? {
        var deepest: String? = null
        var current: Throwable? = error
        val seen = mutableSetOf<Throwable>()
        while (current != null && seen.add(current)) {
            current.message?.takeIf { it.isNotBlank() }?.let { deepest = it }
            current = current.cause
        }
        return deepest?.takeIf { it != directMessageOf(error) }
    }

    fun get(event: Event): String? = errors[event]
}
