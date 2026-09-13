package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksAutoIncrementManager
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowMutation
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.InsertIfAbsent
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.rocksdb.WriteBatch
import org.rocksdb.WriteOptions

/** Inserts one row unless its primary key already exists. */
class RocksInsertIfAbsent(
    values: Map<String, Any?>,
    override val database: RocksdbDatabase
) : InsertIfAbsent(values), RocksQuery {

    override suspend fun execute(table: Table): WriteResult =
        database.withMutationLock {
            RocksRowMutation.requireKnownColumns(table, values)
            val db = requireNotNull(database.database) { "Database is not connected" }
            val columnFamily = requireNotNull(database.columnFamilyHandles[table.name]) {
                "Column family for table `${table.name}` not found"
            }
            val primaryKey = RocksInsertPrimaryKeyResolver.resolve(
                values,
                table,
                readHighWaterMark(table)
            )
            val rowValues = values.toMutableMap().apply {
                table.primaryKey?.let { this[it.name] = primaryKey.value }
            }
            RocksRowMutation.validate(table, rowValues)
            val key = if (table.primaryKey == null) {
                RocksRowKeyEncoder.encodeGeneratedRowKey()
            } else {
                RocksRowKeyEncoder.encodePrimaryKey(table, requireNotNull(primaryKey.value))
            }

            if (table.primaryKey != null && db.keyExists(columnFamily, key)) {
                return@withMutationLock WriteResult(affectedCount = 0)
            }

            WriteBatch().use { batch ->
                batch.put(columnFamily, key, RocksRowValueCodec.forTable(table).encodeRow(rowValues))
                val autoIncrement = table.primaryKey?.takeIf { it.isAutoIncrement }
                if (autoIncrement != null) {
                    RocksAutoIncrementManager.putCounter(
                        batch,
                        database,
                        table,
                        autoIncrement.name,
                        requireNotNull(primaryKey.highWaterMark)
                    )
                }
                WriteOptions().use { options -> db.write(options, batch) }
            }
            WriteResult(affectedCount = 1)
        }

    private fun readHighWaterMark(table: Table): Long? =
        table.primaryKey
            ?.takeIf { it.isAutoIncrement }
            ?.let { RocksAutoIncrementManager.getCurrent(database, table, it.name) }
}
