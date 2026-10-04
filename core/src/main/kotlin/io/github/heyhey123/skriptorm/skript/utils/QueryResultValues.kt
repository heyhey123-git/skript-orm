package io.github.heyhey123.skriptorm.skript.utils

import io.github.heyhey123.skriptorm.type.ConversionWork
import io.github.heyhey123.skriptorm.type.DeferredDatabaseValue

/** Resolves detached values after the query's cursor has closed. */
internal object QueryResultValues {
    suspend fun resolve(rows: Map<String, Any?>): Map<String, Any?> {
        val pending = rows.filterValues { it is DeferredDatabaseValue }
        if (pending.isEmpty()) return rows
        val resolved = ConversionWork.executeBatch(
            pending.values.map { value ->
                { (value as DeferredDatabaseValue).resolve() }
            }
        )
        val result = LinkedHashMap(rows)
        pending.keys.forEachIndexed { index, name -> result[name] = resolved[index] }
        return result
    }
}
