package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksAutoIncrementManager
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksAutoIncrementValueAdapter
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table

/** Resolves primary keys and auto-increment high-water marks for insert queries. */
internal object RocksInsertPrimaryKeyResolver {

    /** Resolved primary-key value and the high-water mark to persist. */
    data class Result(
        val value: Any?,
        val highWaterMark: Long?
    )

    /**
     * Resolves an explicit, generated, or missing primary key.
     * Explicit auto-increment values can advance, but never lower, the high-water mark.
     */
    fun resolve(
        values: Map<String, Any?>,
        table: Table,
        currentHighWaterMark: Long?
    ): Result {
        val column = table.primaryKey ?: return Result(null, null)
        return when {
            values.containsKey(column.name) -> resolveExplicit(
                values,
                column,
                currentHighWaterMark
            )

            column.isAutoIncrement -> resolveGenerated(column, currentHighWaterMark)

            else -> throw IllegalArgumentException(
                "Primary key value for column ${column.name} is missing"
            )
        }
    }

    private fun resolveExplicit(
        values: Map<String, Any?>,
        column: Column<*>,
        currentHighWaterMark: Long?
    ): Result {
        val value = values[column.name]
            ?: throw IllegalArgumentException("Primary key ${column.name} value can't be null")
        if (!column.isAutoIncrement) return Result(value, null)

        val sequenceValue = RocksAutoIncrementValueAdapter.toSequenceValue(column, value)
        return Result(
            value,
            RocksAutoIncrementManager.advanceToAtLeast(
                requireNotNull(currentHighWaterMark),
                sequenceValue
            )
        )
    }

    private fun resolveGenerated(
        column: Column<*>,
        currentHighWaterMark: Long?
    ): Result {
        val next = RocksAutoIncrementManager.nextAfter(requireNotNull(currentHighWaterMark))
        return Result(
            RocksAutoIncrementValueAdapter.toColumnValue(column, next),
            next
        )
    }
}
