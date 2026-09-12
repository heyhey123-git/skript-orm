package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.result.WriteResult

/**
 * Delete an entity by its ID.
 *
 * @param id the ID of the entity to be deleted
 */
abstract class DeleteById(
    val id: Any
) : Query<WriteResult>()
