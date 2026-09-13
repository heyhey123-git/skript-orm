package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.result.WriteResult

/**
 * Update the records matched the where clause
 *
 * @param values the values to be updated
 * @param limit the maximum number of records to be updated
 * @param where the where clause to filter records to be updated
 */
abstract class Update(
    values: Map<String, Any?>,
    val limit: Int?,
    val where: WhereClause?
) : Query<WriteResult>() {
    val values: Map<String, Any?> = values.toMap()
}
