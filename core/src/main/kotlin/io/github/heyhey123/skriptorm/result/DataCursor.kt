package io.github.heyhey123.skriptorm.result

import io.github.heyhey123.skriptorm.type.ConversionThread
import io.github.heyhey123.skriptorm.type.DataType
import java.lang.AutoCloseable

/**
 * Closeable, forward-only cursor over query results.
 *
 * The cursor starts before the first row. Call [next] before reading values and close the cursor
 * after use to release implementation-owned resources. Behavior after [close] is implementation-defined.
 */
interface DataCursor : AutoCloseable {

    /**
     * Moves the cursor to the next row in the result set.
     * Initially, the cursor is positioned before the first row.
     *
     * @return True if there is a next row, false otherwise.
     */
    fun next(): Boolean

    /**
     * Retrieves the value of the specified column as the specified SQL type.
     *
     * @param T The Kotlin type.
     * @param column The name of the column.
     * @param dataType The SQL type to use for conversion.
     * @return The value of the column converted to the specified type, or null if the value is SQL NULL.
     * Callers must honor the converter's readThread. Delayed Skript queries use getDetached instead.
     */
    fun <T : Any> get(column: String, dataType: DataType<T>): T?

    /**
     * Retrieves the value of the specified column index as the specified SQL type.
     *
     * @param T The Kotlin type.
     * @param index The index of the column (1-based, as per JDBC standard).
     * @param dataType The SQL type to use for conversion.
     * @return The value of the column converted to the specified type, or null if the value is SQL NULL.
     * Callers must honor the converter's readThread. Delayed Skript queries use getDetached instead.
     */
    fun <T : Any> get(index: Int, dataType: DataType<T>): T?

    /**
     * Reads a value that remains valid after this cursor closes. Backend implementations postpone
     * server-thread conversions by returning a DeferredDatabaseValue instead of constructing an object.
     * Custom cursors must override this method if their typed reads perform such conversions.
     */
    fun getDetached(column: String, dataType: DataType<*>): Any? {
        require(dataType.converter.readThread == ConversionThread.ANY) {
            "This cursor must implement detached reads for server-thread converters."
        }
        return get(column, dataType)
    }

    /** Index-based counterpart of [getDetached], using the same 1-based column numbering as [get]. */
    fun getDetached(index: Int, dataType: DataType<*>): Any? {
        require(dataType.converter.readThread == ConversionThread.ANY) {
            "This cursor must implement detached reads for server-thread converters."
        }
        return get(index, dataType)
    }
}
