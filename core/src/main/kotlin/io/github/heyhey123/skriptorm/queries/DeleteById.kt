package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.result.WriteResult

/**
 * Delete an entity by its ID.
 *
 * @param id the ID of the entity to be deleted
 */
abstract class DeleteById(
    val id: Any
) : Query<WriteResult>()
