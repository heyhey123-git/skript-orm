package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.DeleteById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table

class RocksDeleteById(
    id: Any,
    override val database: RocksdbDatabase
): DeleteById(id), RocksQuery {
    override suspend fun execute(table: Table): WriteResult {
        val db = database.database!!
        val primaryKey = RocksRowKeyEncoder.encodePrimaryKey(table, id)
        val cfHandle = database.columnFamilyHandles[table.name]
            ?: error("Column family for table `${table.name}` not found, did you forget to register the table?")

        val valueExists = db.keyExists(cfHandle, primaryKey)
        if (!valueExists) {
            return WriteResult(affectedCount = 0)
        }

        db.delete(cfHandle, primaryKey)

        return WriteResult(affectedCount = 1)
    }
}
