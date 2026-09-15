package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.result.RocksDataCursor
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.SelectById
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table

class RocksSelectById(
    id: Any,
    override val database: RocksdbDatabase
) : SelectById(id), RocksQuery {

    override suspend fun execute(table: Table): CursorResult {
        val primaryKey = RocksRowKeyEncoder.encodePrimaryKey(table, id)
        val cfHandle = database.columnFamilyHandles[table.name]
            ?: error("Column family for table `${table.name}` not found, did you forget to register the table?")
        val valueBytes: ByteArray? = database.database!!.get(cfHandle, primaryKey)
            ?: error("Row with primary key `$id` not found in table `${table.name}`")
        val row = RocksRowValueCodec.forTable(table).decodeRow(valueBytes!!)

        return CursorResult(RocksDataCursor(listOf(row)))
    }
}
