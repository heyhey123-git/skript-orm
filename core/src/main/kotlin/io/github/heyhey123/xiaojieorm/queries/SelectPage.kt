package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.result.CursorResult

/**
 * Selects the 1-based page [pageIndex] with [pageSize] positive items, filtered by [where].
 * A null clause applies no filter. Implementations may require a primary key to provide stable order.
 *
 * @throws IllegalArgumentException if [pageSize] is not positive or [pageIndex] is less than one
 */
abstract class SelectPage(
    val pageSize: Int,
    val pageIndex: Int,
    val where: WhereClause?
): Query<CursorResult>() {
    init {
        require(pageSize > 0) { "Page size must be positive." }
        require(pageIndex >= 1) { "Page index must be at least one." }
    }
}
