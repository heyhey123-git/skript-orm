package io.github.heyhey123.skriptorm.impl.mongo.type

import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.ValueConverter
import org.bson.Document

/**
 * Converts the domain values a query carries into the storage values a document holds, which is what the
 * JDBC implementation's binder does for a statement.
 *
 * MongoDB stores what the type's converter produces — a UUID as the bytes of its two halves, a date as its
 * milliseconds, a float as a double — so both sides of a query have to agree on that representation. A
 * filter built from the domain value compares a UUID against bytes and matches nothing, and a document
 * written with one makes the read path convert a value that was never converted. Neither is visible
 * without a test, which is why this is one helper rather than a line in each query.
 */
internal object MongoValues {

    /**
     * [value] as the column [columnName] of [table] stores it, or `null` for a null.
     *
     * @throws IllegalArgumentException if the table has no such column, or the value is not of the
     *   column's domain type. Both are reported the same way the JDBC implementation reports them, so a
     *   script that names a column that does not exist, or writes a string into an integer column, fails
     *   the same wherever it runs.
     */
    fun storage(table: Table, columnName: String, value: Any?): Any? {
        if (value == null) return null
        val type = column(table, columnName).type
        require(type.domainType.isInstance(value)) {
            "Value for type ${type.typeCode} must be ${type.domainType.name}, but was ${value.javaClass.name}."
        }
        @Suppress("UNCHECKED_CAST")
        val converter = type.converter as ValueConverter<Any, Any>
        return converter.toStorage(value)
    }

    /** Every entry of [values], as its column stores it. */
    fun storageDocument(table: Table, values: Map<String, Any?>): Document = Document(
        values.entries.associate { (columnName, value) -> columnName to storage(table, columnName, value) }
    )

    private fun column(table: Table, columnName: String) =
        table.getColumnByName(columnName)
            ?: throw IllegalArgumentException("Table ${table.name} does not have column $columnName.")
}
