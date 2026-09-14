package io.github.heyhey123.xiaojieorm.skript.utils

import org.bukkit.event.Event
import java.util.Collections
import java.util.WeakHashMap

/**
 * A utility object to store and retrieve database error messages associated with Skript events.
 * This is useful for capturing and displaying error messages when database operations fail during event handling.
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
