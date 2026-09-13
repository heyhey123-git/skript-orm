package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.queries.DeleteById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table

class RocksDeleteById(
    id: Any,
    override val database: RocksdbDatabase
) : DeleteById(id), RocksQuery {

    override suspend fun execute(table: Table): WriteResult =
        database.withMutationLock {
            val db = requireNotNull(database.database) { "Database is not connected" }
            val columnFamily = requireNotNull(database.columnFamilyHandles[table.name]) {
                "Column family for table `${table.name}` not found"
            }
            val key = RocksRowKeyEncoder.encodePrimaryKey(table, id)
            if (!db.keyExists(columnFamily, key)) {
                return@withMutationLock WriteResult(0)
            }
            db.delete(columnFamily, key)
            WriteResult(affectedCount = 1)
        }
}
