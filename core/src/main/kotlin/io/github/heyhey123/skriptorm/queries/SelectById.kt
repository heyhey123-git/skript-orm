package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.result.CursorResult

/**
 * Selects an entity by its unique identifier.
 *
 * @property id The unique identifier of the entity.
 */
abstract class SelectById(
    val id: Any
) : Query<CursorResult>()
