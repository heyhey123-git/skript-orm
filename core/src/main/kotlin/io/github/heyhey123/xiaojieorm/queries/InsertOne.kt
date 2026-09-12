package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.result.WriteResult

/**
 * Insert one entity into the table.
 *
 * @param values the entity values to insert
 */
abstract class InsertOne(
    val values: Map<String, Any?>
) : Query<WriteResult>()
