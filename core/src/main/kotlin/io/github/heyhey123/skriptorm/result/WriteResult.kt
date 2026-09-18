package io.github.heyhey123.skriptorm.result

/**
 * Result of a write operation.
 *
 * @property affectedCount Number of affected rows reported by the backend.
 * @property countExact Whether [affectedCount] is exact; false when the backend reports successful
 * writes without an exact per-operation count.
 *
 * What "affected" means is the backend's answer, not this addon's: a delete counts rows removed, an
 * insert counts rows added, and an update counts the rows the backend considers written, which for
 * some backends is the rows it changed and for others the rows it matched. A statement that reports an
 * exact count is what the `store affected rows` clause hands to a script.
 */
data class WriteResult(
    val affectedCount: Long,
    val countExact: Boolean = true
) : ExecutionResult {

    override fun asCursorOrNull() = null

    override fun asWriteResultOrNull() = this
}
