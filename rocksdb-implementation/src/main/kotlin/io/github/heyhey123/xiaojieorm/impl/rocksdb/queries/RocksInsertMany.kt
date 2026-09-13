package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksAutoIncrementManager
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksAutoIncrementValueAdapter
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.InsertMany
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.rocksdb.WriteBatch
import org.rocksdb.WriteOptions

class RocksInsertMany(
    valuesList: List<Map<String, Any?>>,
    override val database: RocksdbDatabase
) : InsertMany(valuesList), RocksQuery {

    override suspend fun execute(table: Table): WriteResult {
        val db = database.database!!
        val tableName = table.name
        val cfHandle = database.columnFamilyHandles[tableName]
            ?: throw IllegalStateException("Column family for table $tableName not found")

        val pkColumn = table.primaryKey

        val mutValuesList = this.valuesList.map { it.toMutableMap() }

        if (pkColumn?.isAutoIncrement == true) {
            val needAutoIncrementCount = mutValuesList.count { pkColumn.name !in it }
            if (needAutoIncrementCount > 0) {
                val idRange = RocksAutoIncrementManager.getAndIncrementBatch(
                    database, table, pkColumn.name, needAutoIncrementCount
                )
                var currentId = idRange.first
                mutValuesList.forEach { row ->
                    if (pkColumn.name !in row) {
                        row[pkColumn.name] = RocksAutoIncrementValueAdapter.toColumnValue(pkColumn, currentId++)
                    }
                }
            }
        }

        WriteBatch().use { writeBatch ->
            WriteOptions().use { writeOptions ->
                for (row in mutValuesList) {
                    val keyBytes = buildRowKey(row, table)
                    val valueBytes = buildRowValue(row, table)
                    writeBatch.put(cfHandle, keyBytes, valueBytes)
                }
                db.write(writeOptions, writeBatch)
            }
        }

        return WriteResult(mutValuesList.size)
    }

    private fun buildRowKey(
        row: MutableMap<String, Any?>,
        table: Table,
    ): ByteArray {
        val pkColumn = table.primaryKey
        val pkValue = pkColumn?.let {
            row[it.name] ?: throw IllegalArgumentException("Primary key value missing")
        }

        val key = pkColumn?.let {
            RocksRowKeyEncoder.encodePrimaryKey(table, pkValue!!)
        } ?: RocksRowKeyEncoder.encodeGeneratedRowKey()

        return key
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildRowValue(
        row: MutableMap<String, Any?>,
        table: Table
    ): ByteArray {

        table.columns.forEach { (name, column) ->
            val value = row[name]

            require(value != null || column.isNullable) {
                "Column $name cannot be null"
            }

            row[name] = value
        }

        return RocksRowValueCodec.forTable(table).encodeRow(row)
    }
}
