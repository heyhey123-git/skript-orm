package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.result.CursorResult

/** Selects every entity matching [where]; a null clause applies no filter. */
abstract class SelectMany(
    val where: WhereClause?
) : Query<CursorResult>()
