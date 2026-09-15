package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.result.WriteResult

/**
 * Updates entities matching [where]; a null clause targets every entity.
 *
 * [values] is snapshotted at construction. A non-null [limit] must be positive when validated by
 * the implementation, and limited updates may be unsupported by some backends.
 */
abstract class Update(
    values: Map<String, Any?>,
    val limit: Int?,
    val where: WhereClause?
) : Query<WriteResult>() {
    val values: Map<String, Any?> = values.toMap()
}
