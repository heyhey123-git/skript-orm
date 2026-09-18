package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.classes.Changer.ChangeMode
import ch.njol.skript.lang.Variable
import org.bukkit.event.Event

object VariableModifier {

    /** Deletes all entries under [variable]. */
    fun clear(variable: Variable<*>, event: Event?) {
        variable.change(event, null, ChangeMode.DELETE)
    }

    /** Clears [variable], then writes each map key as a Skript list index. */
    fun writeMap(variable: Variable<*>, event: Event?, value: Map<String, Any?>) {
        clear(variable, event)
        if (value.isEmpty()) return

        val delta = value.values.toTypedArray()
        val keys = value.keys.toTypedArray()
        variable.change(event, delta, ChangeMode.SET, keys)
    }

    /**
     * Writes a single [value] into [variable], replacing whatever it held.
     *
     * Unlike [writeMap] this does not clear first: a variable that holds one value is replaced by the
     * `SET`, and clearing it separately would only add a step a script could observe in between.
     */
    fun writeValue(variable: Variable<*>, event: Event?, value: Any?) {
        variable.change(event, arrayOf(value), ChangeMode.SET)
    }
}
