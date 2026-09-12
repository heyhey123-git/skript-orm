package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.result.WriteResult

/**
 * Insert one entity into the table if it does not already exist.
 *
 * @param values the values to insert
 */
abstract class InsertIfAbsent(
    val values: Map<String, Any?>
) : Query<WriteResult>()
