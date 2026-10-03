package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.skript.utils.RowLimit
import io.github.heyhey123.skriptorm.table.Table

/**
 * Inserts [rows] in order, splitting large batches into statements of at most
 * `RowLimit.rowsPerStatement(table.columns.size)` rows. This limits the number of bound values
 * per statement; all rows have already been resolved before this function runs.
 *
 * Returns the sum of affected-row counts. The count is exact only if every statement reports an
 * exact count. Earlier statements remain applied if a later one fails, unless the caller runs the
 * operation in a transaction.
 *
 * @param table the table to insert into
 * @param rows the rows to insert, each a column-to-value map
 * @return the summed result of the statements that carried the rows
 */
internal suspend fun Queries.insertManyInStatements(
    table: Table,
    rows: List<Map<String, Any?>>
): WriteResult {
    val perStatement = RowLimit.rowsPerStatement(table.columns.size)
    if (rows.size <= perStatement) return insertMany(rows).execute(table)

    var affected = 0L
    var countExact = true
    for (statement in rows.chunked(perStatement)) {
        val result = insertMany(statement).execute(table)
        affected = Math.addExact(affected, result.affectedCount)
        countExact = countExact && result.countExact
    }
    return WriteResult(affected, countExact)
}
