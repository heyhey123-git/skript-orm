package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.result.CursorResult

/**
 * Selects at most one entity matching [where]. A null clause applies no filter.
 * Which matching entity is returned is implementation-defined unless the backend provides an order.
 */
abstract class SelectOne(
    val where: WhereClause?
) : Query<CursorResult>()
