package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.result.CursorResult

/**
 * Selects the entities matching [where]; a null clause applies no filter.
 *
 * [limit] is the most rows the server is asked for, or null for every match. A caller whose own storage has
 * a ceiling asks for one row more than it accepts, which is how "more than the ceiling" is told from
 * "exactly the ceiling" without reading the rest of the table.
 */
abstract class SelectMany(
    val where: WhereClause?,
    val limit: Int?
) : Query<CursorResult>()
