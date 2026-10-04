package io.github.heyhey123.skriptorm.skript.utils

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.table.Table

/**
 * Reads a multi-row select into the keys a Skript list variable is stored under, refusing a result larger
 * than one statement may store.
 *
 * The section form and the form written without a colon have to behave identically, so the ceiling and the
 * shape of the result live here rather than in each of them. [RowLimit] owns the ceiling and the wording of
 * the refusal.
 */
internal object SelectResult {

    /**
     * Runs `select many` for [where] and returns the `rowIndex::columnName` keys, one-based and present even
     * for a single row.
     *
     * The server is asked for one row past the ceiling: reading that row is how this side learns the table
     * holds more than it may store, without asking for the rest of it. Asking for the ceiling itself would
     * not do, because a result of exactly that many rows is indistinguishable from a larger one that was cut
     * off at the ceiling.
     *
     * @throws TooManyRowsException when the table matched more rows than [RowLimit.ROWS].
     */
    suspend fun readMany(queries: Queries, table: Table, where: WhereClause?): Map<String, Any?> {
        val result = linkedMapOf<String, Any?>()
        var rowIndex = 1
        queries.selectMany(where, RowLimit.PROBE_ROWS).execute(table).cursor.use { cursor ->
            while (cursor.next()) {
                table.columns.values.forEach { column ->
                    result["$rowIndex::${column.name}"] = cursor.getDetached(column.name, column.type)
                }
                rowIndex++
            }
        }
        if (rowIndex - 1 > RowLimit.ROWS) throw TooManyRowsException(RowLimit.readRefusal(table.name))
        return result
    }
}
