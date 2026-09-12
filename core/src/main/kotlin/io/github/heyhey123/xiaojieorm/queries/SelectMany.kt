package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.result.CursorResult

/**
 * Select multiple entities.
 *
 * @property where The WHERE clause defining the selection condition.
 */
abstract class SelectMany(
    val where: WhereClause?
) : Query<CursorResult>()
