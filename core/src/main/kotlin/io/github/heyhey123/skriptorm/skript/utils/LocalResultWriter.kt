package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.lang.Variable
import ch.njol.skript.registrations.Classes
import ch.njol.skript.variables.Variables
import io.github.heyhey123.skriptorm.type.ConversionWork
import io.github.heyhey123.skriptorm.type.DeferredDatabaseValue
import io.github.heyhey123.skriptorm.utils.BenchmarkTimings
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

/** Writes into an isolated local scope while its original trigger is parked. */
internal class LocalResultWriter private constructor(private val path: String, private val safeStoredClasses: Set<Class<*>>) {
    private val carrier = Carrier()

    companion object {
        private val safeClasses = setOf(
            String::class.java, Boolean::class.javaObjectType, Byte::class.javaObjectType,
            Short::class.javaObjectType, Int::class.javaObjectType, Long::class.javaObjectType,
            Float::class.javaObjectType, Double::class.javaObjectType, UUID::class.java
        )

        /** Resolve expressions and warm Skript's class-info cache before leaving the server thread. */
        fun capture(variable: Variable<*>, event: Event): LocalResultWriter? {
            if (!variable.isLocal || !variable.isList) return null
            val safeStoredClasses = safeClasses.filterTo(HashSet()) {
                Classes.getSuperClassInfo(it).serializeAs == null
            }
            val path = variable.name.toString(event)
            require(path.endsWith("::*")) { "Invalid local result variable: $path" }
            return LocalResultWriter(path, safeStoredClasses)
        }
    }

    suspend fun write(locals: Any?, rows: Map<String, Any?>): Any? {
        SkriptLocalVariables.restore(carrier, locals)
        try {
            val sync = ArrayList<() -> Unit>()
            BenchmarkTimings.async(BenchmarkTimings.Stage.RESULT) {
                Variables.setVariable(path, null, carrier, true)
                val base = path.dropLast(1)
                for ((key, value) in rows) {
                    val name = "$base$key"
                    if (value == null || value.javaClass in safeStoredClasses) {
                        Variables.setVariable(name, value, carrier, true)
                    } else {
                        sync.add {
                            val resolved = if (value is DeferredDatabaseValue) value.resolve() else value
                            if (resolved is Array<*>) {
                                resolved.forEachIndexed { index, child ->
                                    Variables.setVariable("$name::${index + 1}", child, carrier, true)
                                }
                            } else {
                                Variables.setVariable(name, resolved, carrier, true)
                            }
                        }
                    }
                }
            }
            ConversionWork.executeBatch(sync)
            return SkriptLocalVariables.remove(carrier)
        } finally {
            SkriptLocalVariables.clear(carrier)
        }
    }

    private class Carrier : Event(true) {
        override fun getHandlers(): HandlerList = HANDLERS
        companion object {
            private val HANDLERS = HandlerList()
        }
    }
}
