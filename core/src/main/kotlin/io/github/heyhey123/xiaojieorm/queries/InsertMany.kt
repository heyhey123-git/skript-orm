package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.result.WriteResult

/**
 * Insert multiple entities into the table.
 *
 * @constructor
 *
 * @param valuesList
 */
abstract class InsertMany(
    valuesList: List<Map<String, Any?>>
) : Query<WriteResult>() {
    val valuesList: List<Map<String, Any?>> = valuesList.map { it.toMap() }
}
