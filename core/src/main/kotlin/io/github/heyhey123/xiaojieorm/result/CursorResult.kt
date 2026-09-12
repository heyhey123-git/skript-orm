package io.github.heyhey123.xiaojieorm.result

/**
 * Cursor result of a query execution.
 *
 * @property cursor The data cursor containing the query results.
 */
data class CursorResult(
    val cursor: DataCursor
): ExecutionResult {

    override fun asCursorOrNull(): DataCursor = cursor

    override fun asWriteResultOrNull() = null
}
