package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.result.WriteResult

/**
 * Deletes entities matching [where]; a null clause targets every entity.
 * Limited deletes may be unsupported by some backends.
 */
abstract class Delete(
    val limit: Int?,
    val where: WhereClause?
) : Query<WriteResult>()
