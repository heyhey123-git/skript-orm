package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.result.WriteResult

/**
 * Delete the entities matched the where clause
 *
 * @param limit the maximum number of records to be deleted
 * @param where the where clause to filter records to be deleted
 */
abstract class Delete(
    val limit: Int?,
    val where: WhereClause?
) : Query<WriteResult>()
