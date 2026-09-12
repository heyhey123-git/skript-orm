package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.table.Table
import org.rocksdb.ColumnFamilyHandle
import org.rocksdb.RocksDB

/**
 * Rocks query helper
 *
 */
object RocksQueryHelper {

    /**
     * Lookup a single record by primary key
     *
     * @param table The table to lookup in
     * @param pkValue The primary key value
     * @param database The database instance
     * @param cfHandle The column family handle to scan
     * @return The row data as a map, or null if not found
     */
    fun lookupByPrimaryKey(
        table: Table,
        pkValue: Any,
        database: RocksDB,
        cfHandle: ColumnFamilyHandle
    ): Map<String, Any?>? {
        val key = RocksRowKeyEncoder.encodePrimaryKey(table, pkValue)
        val value = database.get(cfHandle, key) ?: return null
        val codec = RocksRowValueCodec.forTable(table)
        return codec.decodeRow(value)
    }

    /**
     * Scan the column family with a filter predicate.
     *
     * @param table The table to scan for
     * @param predicate The filter predicate to apply to each row
     * @param database The database instance
     * @param cfHandle The column family handle to scan
     * @param limit Optional limit on the number of results to return
     * @param skip Number of matching records to skip before collecting results
     * @return A list of rows matching the predicate
     */
    fun scanWithFilter(
        table: Table,
        predicate: (Map<String, Any?>) -> Boolean,
        database: RocksDB,
        cfHandle: ColumnFamilyHandle,
        limit: Int? = null,
        skip: Int = 0
    ): List<Map<String, Any?>> {
        val result = mutableListOf<Map<String, Any?>>()
        val codec = RocksRowValueCodec.forTable(table)

        var skippedCount = 0

        database.newIterator(cfHandle).use { iterator ->
            iterator.seekToFirst()
            while (iterator.isValid) {
                if (limit != null && result.size >= limit) {
                    break
                }

                val row = codec.decodeRow(iterator.value())
                if (!predicate(row)) {
                    iterator.next()
                    continue
                }

                if (skippedCount < skip) {
                    skippedCount++
                    iterator.next()
                    continue
                }

                result.add(row)
                iterator.next()
            }
        }

        return result
    }
}
