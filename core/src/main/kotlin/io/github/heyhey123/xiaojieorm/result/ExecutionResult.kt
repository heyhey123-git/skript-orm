package io.github.heyhey123.xiaojieorm.result

/**
 * Execution result, which can be either a DataCursor or a WriteResult.
 *
 */
interface ExecutionResult {

    /**
     * Converts the result to a DataCursor if possible.
     *
     * @return The DataCursor or null if the result is not a cursor.
     */
    fun asCursorOrNull(): DataCursor?

    /**
     * Converts the result to a WriteResult if possible.
     *
     * @return The WriteResult or null if the result is not a write result.
     */
    fun asWriteResultOrNull(): WriteResult?
}
