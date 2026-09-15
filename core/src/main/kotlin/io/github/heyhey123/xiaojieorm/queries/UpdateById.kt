package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.result.WriteResult

/**
 * Update an entity by its unique identifier.
 *
 * @property id The unique identifier of the entity.
 * @property values The values to be updated.
 */
abstract class UpdateById(
    val id: Any,
    values: Map<String, Any?>
) : Query<WriteResult>() {

    val values: Map<String, Any?> = values.toMap()
}
