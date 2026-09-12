package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.result.CursorResult

/**
 * Selects a single entity based on a specified condition.
 *
 * @property where The WHERE clause defining the selection condition.
 */
abstract class SelectOne(
    val where: WhereClause?
) : Query<CursorResult>()
