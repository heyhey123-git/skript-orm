package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.InsertIfAbsent
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table

class RocksInsertIfAbsent(
    values: Map<String, Any?>,
    override val database: RocksdbDatabase
) : InsertIfAbsent(values), RocksQuery {
    override suspend fun execute(table: Table): WriteResult {
        val cfHandle = database.columnFamilyHandles[table.name]
            ?: throw IllegalStateException("Column family for table ${table.name} not found")

        val pkColumn = table.primaryKey

        val pkValue = pkColumn?.let { values[it.name] }

        val rowExists = (pkValue != null && RocksQueryHelper.lookupByPrimaryKey(
            table,
            pkValue,
            database.database!!,
            cfHandle
        ) != null) ||
                RocksQueryHelper.scanWithFilter(
                    table,
                    { row -> values.none { (key, value) -> row[key] == value } },
                    database.database!!,
                    cfHandle,
                    limit = 1
                ).isNotEmpty()

        return if (rowExists) {
            WriteResult(affectedCount = 0)
        } else {
            // Proceed with insertion
            val key = pkColumn?.let {
                RocksRowKeyEncoder.encodePrimaryKey(table, pkValue!!)
            } ?: RocksRowKeyEncoder.encodeGeneratedRowKey()

            val value = RocksRowValueCodec.forTable(table).encodeRow(values)

            database.database!!.put(cfHandle, key, value)
            WriteResult(affectedCount = 1)
        }
    }
}
