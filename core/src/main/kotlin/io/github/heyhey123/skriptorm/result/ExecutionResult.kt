package io.github.heyhey123.skriptorm.result

/** Common result type for query execution. Use the projection methods to inspect its variant. */
interface ExecutionResult {

    /** Returns the cursor when this is a [CursorResult], otherwise `null`. */
    fun asCursorOrNull(): DataCursor?

    /** Returns this result when it is a [WriteResult], otherwise `null`. */
    fun asWriteResultOrNull(): WriteResult?
}
