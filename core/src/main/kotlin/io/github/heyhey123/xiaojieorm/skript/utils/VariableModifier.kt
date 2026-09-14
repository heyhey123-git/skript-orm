package io.github.heyhey123.xiaojieorm.skript.utils

import ch.njol.skript.classes.Changer.ChangeMode
import ch.njol.skript.lang.Variable
import org.bukkit.event.Event

object VariableModifier {
    fun clear(variable: Variable<*>, event: Event?) {
        variable.change(event, null, ChangeMode.DELETE)
    }

    fun writeMap(variable: Variable<*>, event: Event?, value: Map<String, Any?>) {
        clear(variable, event)
        if (value.isEmpty()) return

        val delta = value.values.toTypedArray()
        val keys = value.keys.toTypedArray()
        variable.change(event, delta, ChangeMode.SET, keys)
    }
}
