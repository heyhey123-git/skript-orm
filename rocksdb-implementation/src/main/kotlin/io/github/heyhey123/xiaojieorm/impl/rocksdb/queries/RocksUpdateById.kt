package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.UpdateById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table

class RocksUpdateById(
    id: Any,
    values: Map<String, Any?>,
    override val database: RocksdbDatabase
): UpdateById(id, values), RocksQuery {
    override suspend fun execute(table: Table): WriteResult {
        val primaryKey = RocksRowKeyEncoder.encodePrimaryKey(table, id)
        val cfHandle = database.columnFamilyHandles[table.name]
            ?: error("Column family for table `${table.name}` not found, did you forget to register the table?")
        val keyExists = database.database!!.keyExists(cfHandle, primaryKey)
        if (!keyExists) {
            return WriteResult(affectedCount = 0)
        }
        val valueBytes = RocksRowValueCodec.forTable(table).encodeRow(values)

        database.database!!.put(cfHandle, primaryKey, valueBytes)
        return WriteResult(affectedCount = 1)
    }
}
