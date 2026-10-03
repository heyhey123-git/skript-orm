package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.lang.Variable
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.NumericValues
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.nbt.NbtSupport
import org.bukkit.event.Event
import org.skriptlang.skript.lang.converter.Converters

/**
 * Reads the values of a write section from a Skript list variable.
 *
 * Accepts the same shape as select results, so selected rows can be written back directly:
 *
 * ```
 * {_row::columnName}                single row, keyed by column name
 * {_rows::rowIndex::columnName}     rows, keyed by one-based row index and column name
 * ```
 *
 * Skript stores list variables as nested maps, with a node's scalar value under a `null` key.
 * A level whose values are maps contains rows; a level of scalar values contains one row.
 *
 * Reading happens on the main thread while local variables are attached to the event. Values are
 * converted to their column types, as they are for a `values` block.
 *
 * ## Missing columns and SQL NULL
 *
 * Assigning null to a Skript variable removes its key. A list variable therefore cannot express
 * the difference between an omitted column and an explicit SQL NULL. This reader leaves absent
 * keys absent.
 *
 * Each write operation decides how to handle missing columns:
 *
 * - `insert` uses exactly the supplied columns, so an omitted column is absent from the statement
 *   and the database default applies.
 * - `update`, `update by id`, and `upsert by id` change only supplied columns. Omitted columns
 *   stay out of both `SET` and `ON DUPLICATE KEY UPDATE`.
 * - `insert many` is the one exception, described below.
 *
 * To write SQL NULL, use an explicit `null` in a `values` block, such as `nickname: null`.
 * Treating every missing key as null would silently clear columns from dynamic variables, so
 * this reader does not support full-row replacement.
 *
 * ## Why multiple rows are filled differently
 *
 * One statement binds one column list. For multiple rows (`insert many`), this reader collects
 * the union of supplied columns in first-seen order and fills missing values with null. That
 * also allows a select-many result with unset NULL keys to be inserted again. A single row
 * remains sparse so an update changes only its supplied columns.
 */
object VariableValuesReader {

    /**
     * Reads and validates the rows held by [variable] against [table].
     *
     * A returned row is sparse: it holds the columns the variable supplied and nothing else, except
     * when the variable holds several rows, in which case all rows carry the same column set with
     * null for the columns a row omitted. See the class documentation for what an absent column
     * means to each write.
     *
     * @return one map per row, each keyed by column name
     * @throws IllegalArgumentException if the variable is unset or empty, mixes rows with plain
     *         column values, names a column that [table] does not declare, or holds a value that
     *         cannot be converted to the type of its column
     */
    fun read(variable: Variable<*>, table: Table, event: Event?): List<Map<String, Any?>> {
        val raw = variable.getRaw(event) as? Map<*, *>
            ?: throw IllegalArgumentException("$variable is not set.")
        requireKeysOnly(variable, raw)

        val entries = raw.entries.filter { it.key != null }
        require(entries.isNotEmpty()) { "$variable does not hold any values." }

        val nested = entries.first().value is Map<*, *>
        require(entries.all { (it.value is Map<*, *>) == nested }) {
            "$variable mixes rows with plain column values."
        }

        if (!nested) return listOf(readRow(variable, table, raw))

        // Resolve each distinct column once, then build rows against the shared column set.
        val sources = ArrayList<Map<*, *>>(entries.size)
        val columns = LinkedHashMap<String, Column<*>>()
        for (entry in entries) {
            val source = entry.value as Map<*, *>
            requireKeysOnly(variable, source)
            sources += source
            for (key in source.keys) {
                val columnName = key as String
                if (columns.containsKey(columnName)) continue
                columns[columnName] = table.getColumnByName(columnName)
                    ?: throw IllegalArgumentException(
                        "Column '$columnName' does not exist in table '${table.name}'."
                    )
            }
        }

        val rows = ArrayList<Map<String, Any?>>(sources.size)
        for ((index, source) in sources.withIndex()) {
            val row = LinkedHashMap<String, Any?>(columns.size)
            if (index == 0) {
                // The query layer takes the statement's column order from the first row.
                for ((columnName, column) in columns) {
                    row[columnName] = convert(variable, column, source[columnName])
                }
            } else {
                // Preserve each row's supplied values and fill columns it omitted with null.
                for ((key, value) in source) {
                    val columnName = key as String
                    row[columnName] = convert(variable, columns.getValue(columnName), value)
                }
                if (row.size != columns.size) {
                    for (columnName in columns.keys) {
                        if (!row.containsKey(columnName)) row[columnName] = null
                    }
                }
            }
            rows += row
        }
        return rows
    }

    private fun readRow(variable: Variable<*>, table: Table, source: Map<*, *>): Map<String, Any?> {
        requireKeysOnly(variable, source)

        val row = LinkedHashMap<String, Any?>()
        for ((key, value) in source) {
            val columnName = key as String
            val column = table.getColumnByName(columnName)
                ?: throw IllegalArgumentException(
                    "Column '$columnName' does not exist in table '${table.name}'."
                )
            row[columnName] = convert(variable, column, value)
        }
        return row
    }

    /**
     * Rejects scalar values stored alongside keys. Skript stores such values under a `null` key,
     * which cannot be interpreted as a column name or row index.
     */
    private fun requireKeysOnly(variable: Variable<*>, source: Map<*, *>) {
        require(source.keys.none { it == null }) {
            "$variable holds a plain value alongside its keys. Only keys are allowed."
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun convert(
        variable: Variable<*>,
        column: Column<*>,
        value: Any?
    ): Any? {
        if (value == null) return null

        // Detach SkBee compounds from their source objects before the write. Skript's converters
        // cannot convert directly between classes owned by different plugins.
        val compound = NbtSupport.normalize(value)
        if (NbtSupport.isNbt(compound)) return compound

        val domainType = column.type.domainType
        if (NumericValues.isNumeric(domainType)) {
            // Narrow here so out-of-range values are rejected instead of silently converted.
            val number = Converters.convert(value, Number::class.java)
                ?: throw IllegalArgumentException(
                    "The value of '${column.name}' in $variable cannot be read as a number."
                )
            return NumericValues.narrow(column, number)
        }

        return Converters.convert(value, domainType as Class<Any>)
            ?: throw IllegalArgumentException(
                "The value of '${column.name}' in $variable cannot be converted to ${domainType.simpleName}."
            )
    }
}
