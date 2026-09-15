package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.result.WriteResult

/**
 * Inserts multiple entities. The outer list and every row map are snapshotted at construction.
 * Backend implementations define whether an empty batch is accepted and how exact counts are reported.
 */
abstract class InsertMany(
    valuesList: List<Map<String, Any?>>
) : Query<WriteResult>() {

    val valuesList: List<Map<String, Any?>> = valuesList.map { it.toMap() }
}
