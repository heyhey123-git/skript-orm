package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.lang.Variable
import org.bukkit.event.Event
import org.skriptlang.skript.log.runtime.RuntimeErrorProducer

/**
 * Turns the list variable a write takes its values from into rows, the way the write sections do.
 *
 * The read happens on the server thread, because it walks Skript's variables, so it belongs in the walk of
 * whichever element is asking and never in the query that runs off it.
 */
internal object WriteValues {

    /** The rows [variable] holds, shaped like a select result, or null when that cannot be read. */
    fun rows(event: Event, producer: RuntimeErrorProducer, variable: Variable<*>, target: DatabaseWork.Target): List<Map<String, Any?>>? =
        try {
            VariableValuesReader.read(variable, target.table, event)
        } catch (error: Exception) {
            DatabaseWork.report(event, producer, "Failed to parse write values: ${error.message}")
            null
        }

    /** The single row [variable] holds, or null when it holds a different number of them. */
    fun singleRow(
        event: Event,
        producer: RuntimeErrorProducer,
        variable: Variable<*>,
        target: DatabaseWork.Target
    ): Map<String, Any?>? {
        val rows = rows(event, producer, variable, target) ?: return null
        if (rows.size == 1) return rows.first()

        DatabaseWork.report(
            event,
            producer,
            "Failed to parse write values: $variable holds ${rows.size} rows, but this write expects " +
                "exactly one row of values."
        )
        return null
    }
}
