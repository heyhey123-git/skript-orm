package io.github.heyhey123.skriptorm.result

/**
 * Result of a write operation.
 *
 * @property affectedCount Number of affected entities reported by the backend.
 * @property countExact Whether [affectedCount] is exact; false when the backend reports successful
 * writes without an exact per-operation count.
 */
data class WriteResult(
    val affectedCount: Long,
    val countExact: Boolean = true
) : ExecutionResult {

    override fun asCursorOrNull() = null

    override fun asWriteResultOrNull() = this
}
