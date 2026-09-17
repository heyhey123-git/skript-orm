package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.result.CursorResult

/** Selects every entity matching [where]; a null clause applies no filter. */
abstract class SelectMany(
    val where: WhereClause?
) : Query<CursorResult>()
