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
        set(event, error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName)
    }

    fun get(event: Event): String? = errors[event]
}
