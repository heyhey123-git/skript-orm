package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.skript.utils.RowLimit
import io.github.heyhey123.skriptorm.table.Table

/**
 * Inserts every one of [rows] and answers what the server reported for all of them together.
 *
 * A batch is the author's: every row in it was asked for, so every row is written. What a batch larger than
 * one statement may carry changes is how many statements carry it — rows in the order they were given, each
 * statement at most `RowLimit.rowsPerStatement(table.columns.size)` of them — because a driver binds a fixed
 * number of values to a statement and refuses a statement that asks for more. A read is held to a row count
 * so the server thread is not held for seconds; a write is spread over statements so no statement is larger
 * than a driver can take, and because the work is counted in values, the two boundaries are the same line on
 * a six-column table.
 *
 * The count is the sum of the statements, exact only when every one of them reported exactly. A failure part
 * way through leaves the statements before it applied, exactly as it would with any other sequence of
 * statements: whether that is rolled back is the caller's transaction scope, not this loop's.
 *
 * A batch within the boundary is one statement and is passed through untouched.
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
