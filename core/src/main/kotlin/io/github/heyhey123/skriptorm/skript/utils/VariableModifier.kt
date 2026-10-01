package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.classes.Changer.ChangeMode
import ch.njol.skript.lang.Variable
import org.bukkit.event.Event

object VariableModifier {

    /** Deletes all entries under [variable]. */
    fun clear(variable: Variable<*>, event: Event?) {
        variable.change(event, null, ChangeMode.DELETE)
    }

    /**
     * Clears [variable], then writes each map key as a Skript list index.
     *
     * A result large enough to be worth it ([FastVariableStore.MIN_VALUES]) is built on the database thread
     * and attached in one step. Anything unexpected about that path falls back to the loop below, and every
     * result below the threshold uses the loop directly, so nothing small depends on Skript's internals.
     */
    fun writeMap(variable: Variable<*>, event: Event?, value: Map<String, Any?>) {
        if (value.size >= FastVariableStore.MIN_VALUES) {
            FastVariableStore.probe()
            val prepared = FastVariableStore.take(value)
            if (prepared != null && FastVariableStore.attach(variable, event, prepared)) return
        }
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
