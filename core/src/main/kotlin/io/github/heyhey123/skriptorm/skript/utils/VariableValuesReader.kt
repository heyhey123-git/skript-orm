package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.lang.Variable
import io.github.heyhey123.skriptorm.skript.utils.VariableValuesReader.fillMissingColumns
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.nbt.NbtSupport
import org.bukkit.event.Event
import org.skriptlang.skript.lang.converter.Converters

/**
 * Reads the values of a write section from a Skript list variable.
 *
 * The accepted shapes mirror the select result format, so a value produced by a select section can
 * be written back without reshaping it:
 *
 * ```
 * {_row::columnName}                single row, keyed by column name
 * {_rows::rowIndex::columnName}     rows, keyed by one-based row index and column name
 * ```
 *
 * Skript stores a list variable as a nested map, where the scalar value of a node lives under the
 * `null` key of its branch. A level therefore holds rows when every value is itself a map, and a
 * single row when none of them is.
 *
 * Reading happens on the main thread so that local variables are still attached to the event. Values
 * are converted to the domain type of their column, which keeps the variable form as permissive as
 * the expression form used by a `values` block.
 *
 * ## Missing columns and SQL NULL
 *
 * A Skript variable cannot hold null: assigning null to a key removes it. A list variable therefore
 * cannot distinguish a column that the author left out on purpose from one that was meant to be
 * stored as NULL, because both appear as an absent key. The reader does not guess between them and
 * never invents a value for an absent key on its own.
 *
 * Which columns a write then touches is decided by the operation, not by this reader:
 *
 * - `insert` uses exactly the supplied columns, so an omitted column is absent from the statement
 *   and the database default applies.
 * - `update`, `update by id` and `upsert by id` treat the supplied columns as a patch: an omitted
 *   column is not part of the `SET` list and keeps its stored value, and an omitted column is not
 *   part of the `ON DUPLICATE KEY UPDATE` list either.
 * - `insert many` is the one exception, described below.
 *
 * Writing a SQL NULL is therefore impossible through a list variable. The only form that expresses
 * it is the literal `null` in a `values` block, as in a body containing `nickname: null`, which is
 * resolved to a present key holding null. A dedicated full-row replacement mode that would read an
 * absent key as null is deliberately not implemented; if it is ever added it has to be an explicit
 * opt-in, because the same input would otherwise silently clear every column that a dynamic
 * variable happens to miss.
 *
 * ## Why multiple rows are filled differently
 *
 * A single statement binds one column list, so rows taken from a variable must agree on their
 * columns before they reach the query layer. [fillMissingColumns] gives every row the union of the
 * columns by first appearance and writes null wherever a row omitted one. This applies only when
 * more than one row is present, which is exactly the `insert many` payload. A select section leaves
 * the key of a NULL column unset, so filling the union is what makes a select-many result round
 * trip unchanged. A single row is left sparse so that the patch semantics above stay intact.
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

        val rows = if (nested) {
            entries.map { readRow(variable, table, it.value as Map<*, *>) }
        } else {
            listOf(readRow(variable, table, raw))
        }
        return fillMissingColumns(rows)
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
            row[columnName] = convert(variable, columnName, column.type.domainType, value)
        }
        return row
    }

    /**
     * Rejects a level that holds a scalar of its own. Skript keeps such a value under the `null` key
     * of the branch, and because a row and its parent row index are stored the same way, that value
     * cannot be told apart from a column.
     */
    private fun requireKeysOnly(variable: Variable<*>, source: Map<*, *>) {
        require(source.keys.none { it == null }) {
            "$variable holds a plain value alongside its keys. Only keys are allowed."
        }
    }

    /**
     * Gives every row the same column set, ordered by first appearance, because a single statement
     * can only bind one column list. A select section leaves the key of a SQL NULL column unset, so
     * a ragged variable is the normal result of a round trip, and a column that a row omits has to be
     * written as null rather than rejected.
     */
    private fun fillMissingColumns(rows: List<Map<String, Any?>>): List<Map<String, Any?>> {
        if (rows.size == 1) return rows

        val columns = LinkedHashSet<String>()
        rows.forEach { columns.addAll(it.keys) }

        return rows.map { row ->
            LinkedHashMap<String, Any?>(columns.size).apply {
                columns.forEach { column -> put(column, row[column]) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun convert(
        variable: Variable<*>,
        columnName: String,
        domainType: Class<*>,
        value: Any?
    ): Any? {
        if (value == null) return null

        // A compound from SkBee belongs to a live object and only means what it meant when the script
        // named it; normalising replaces it with a detached compound holding the same data. Skript's
        // converters cannot be asked to do this, because it would mean converting between two classes
        // that belong to different plugins.
        val compound = NbtSupport.normalize(value)
        if (NbtSupport.isNbt(compound)) return compound

        return Converters.convert(value, domainType as Class<Any>)
            ?: throw IllegalArgumentException(
                "The value of '$columnName' in $variable cannot be converted to ${domainType.simpleName}."
            )
    }
}
