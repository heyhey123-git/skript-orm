package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.result.CursorResult

/**
 * Select a page of results
 *
 * @param pageSize The number of items per page
 * @param pageIndex The index of the page to select (1-based)
 * @param where The WHERE clause defining the selection condition
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
