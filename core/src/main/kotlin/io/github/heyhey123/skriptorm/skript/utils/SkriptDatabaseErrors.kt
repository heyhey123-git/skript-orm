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
     */
    fun messageOf(error: Throwable): String =
        error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName

    fun get(event: Event): String? = errors[event]
}
