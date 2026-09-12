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
    val valuesList: List<Map<String, Any?>>
) : Query<WriteResult>()
