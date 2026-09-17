package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.result.WriteResult

/**
 * Insert one entity into the table if it does not already exist.
 *
 * @param values the values to insert
 */
abstract class InsertIfAbsent(
    values: Map<String, Any?>
) : Query<WriteResult>() {

    val values: Map<String, Any?> = values.toMap()
}
