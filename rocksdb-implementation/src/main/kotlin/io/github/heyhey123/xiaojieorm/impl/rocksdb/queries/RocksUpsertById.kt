package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksAutoIncrementManager
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksAutoIncrementValueAdapter
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowMutation
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.UpsertById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.rocksdb.WriteBatch
import org.rocksdb.WriteOptions

class RocksUpsertById(
    id: Any,
    values: Map<String, Any?>,
    override val database: RocksdbDatabase
) : UpsertById(id, values), RocksQuery {

    override suspend fun execute(table: Table): WriteResult =
        database.withMutationLock {
            val primaryKeyColumn = table.primaryKey
                ?: error("Table `${table.name}` has no primary key")
            require(values[primaryKeyColumn.name]?.let { it == id } ?: true) {
                "Primary key value in upsert values must match the requested id."
            }
            RocksRowMutation.requireKnownColumns(table, values)

            val db = requireNotNull(database.database) { "Database is not connected" }
            val columnFamily = requireNotNull(database.columnFamilyHandles[table.name]) {
                "Column family for table `${table.name}` not found"
            }
            val key = RocksRowKeyEncoder.encodePrimaryKey(table, id)
            val codec = RocksRowValueCodec.forTable(table)
            val existing = db.get(columnFamily, key)?.let(codec::decodeRow).orEmpty()
            val row = RocksRowMutation.merge(table, existing, values, id)

            WriteBatch().use { batch ->
                batch.put(columnFamily, key, codec.encodeRow(row))
                if (primaryKeyColumn.isAutoIncrement) {
                    val current = RocksAutoIncrementManager.getCurrent(
                        database,
                        table,
                        primaryKeyColumn.name
                    )
                    val requested = RocksAutoIncrementValueAdapter.toSequenceValue(primaryKeyColumn, id)
                    RocksAutoIncrementManager.putCounter(
                        batch,
                        database,
                        table,
                        primaryKeyColumn.name,
                        RocksAutoIncrementManager.advanceToAtLeast(current, requested)
                    )
                }
                WriteOptions().use { options -> db.write(options, batch) }
            }
            WriteResult(affectedCount = 1)
        }
}
