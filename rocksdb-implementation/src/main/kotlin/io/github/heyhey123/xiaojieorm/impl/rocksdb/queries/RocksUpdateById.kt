package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowMutation
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.UpdateById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table

class RocksUpdateById(
    id: Any,
    values: Map<String, Any?>,
    override val database: RocksdbDatabase
) : UpdateById(id, values), RocksQuery {

    override suspend fun execute(table: Table): WriteResult =
        database.withMutationLock {
            if (values.isEmpty()) return@withMutationLock WriteResult(0)
            RocksRowMutation.requireKnownColumns(table, values)
            val primaryKeyColumn = table.primaryKey
                ?: error("Table `${table.name}` has no primary key")
            require(primaryKeyColumn.name !in values) {
                "Updating primary key column `${primaryKeyColumn.name}` is not supported."
            }

            val db = requireNotNull(database.database) { "Database is not connected" }
            val columnFamily = requireNotNull(database.columnFamilyHandles[table.name]) {
                "Column family for table `${table.name}` not found"
            }
            val key = RocksRowKeyEncoder.encodePrimaryKey(table, id)
            val stored = db.get(columnFamily, key)
                ?: return@withMutationLock WriteResult(0)
            val codec = RocksRowValueCodec.forTable(table)
            val merged = RocksRowMutation.merge(table, codec.decodeRow(stored), values)

            db.put(columnFamily, key, codec.encodeRow(merged))
            WriteResult(affectedCount = 1)
        }
}
