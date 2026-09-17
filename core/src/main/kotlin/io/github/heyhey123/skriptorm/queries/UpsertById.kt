package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.result.WriteResult

/**
 * Upsert an entity by its unique identifier.
 *
 * @property id The unique identifier of the entity.
 * @property values The values to be upserted.
 */
abstract class UpsertById(
    val id: Any,
    values: Map<String, Any?>
) : Query<WriteResult>() {

    val values: Map<String, Any?> = values.toMap()
}
