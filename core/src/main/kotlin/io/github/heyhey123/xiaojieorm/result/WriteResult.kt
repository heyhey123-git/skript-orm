package io.github.heyhey123.xiaojieorm.result

/**
 * Write result. Used to represent the outcome of write operations such as insert, update, or delete.
 *
 * @property affectedCount The number of rows affected by the write operation.
 * @property countExact Indicates whether the affected count is exact or not. If false, it means the count may not be accurate.
 */
data class WriteResult(
    val affectedCount: Long,
    val countExact: Boolean = true
) : ExecutionResult {

    override fun asCursorOrNull() = null

    override fun asWriteResultOrNull() = this
}
