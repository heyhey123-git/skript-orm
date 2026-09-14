package io.github.heyhey123.xiaojieorm.result

/**
 * Write result. Used to represent the outcome of write operations such as insert, update, or delete.
 *
 * @property affectedCount The number of rows affected by the write operation.
 */
data class WriteResult(
    val affectedCount: Long
) : ExecutionResult {

    override fun asCursorOrNull() = null

    override fun asWriteResultOrNull() = this
}
