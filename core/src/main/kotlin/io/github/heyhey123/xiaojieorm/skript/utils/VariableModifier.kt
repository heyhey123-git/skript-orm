package io.github.heyhey123.xiaojieorm.skript.utils

import ch.njol.skript.classes.Changer.ChangeMode
import ch.njol.skript.lang.Variable
import org.bukkit.event.Event

object VariableModifier {
    fun writeList(variable: Variable<*>, event: Event?, value: List<Any?>) {
        val delta = value.toTypedArray()
        variable.change(event, delta, ChangeMode.SET)
    }

    fun writeMap(variable: Variable<*>, event: Event?, value: Map<String, Any?>) {
        val delta = value.values.toTypedArray()
        val keys = value.keys.toTypedArray()
        variable.change(event, delta, ChangeMode.SET, keys)
    }
}
