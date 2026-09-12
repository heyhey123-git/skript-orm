package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksAutoIncrementManager
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.InsertOne
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table

class RocksInsertOne(
    values: Map<String, Any?>,
    override val database: RocksdbDatabase
) : InsertOne(values), RocksQuery {
    @Suppress("UNCHECKED_CAST")
    override suspend fun execute(table: Table): WriteResult {
        val cfHandle = database.columnFamilyHandles[table.name]
            ?: throw IllegalStateException("Column family for table ${table.name} not found")

        val pkColumn = table.primaryKey

        val pkValue: Any? = when {
            pkColumn == null -> null // No primary key defined

            values.containsKey(pkColumn.name) -> values[pkColumn.name]
                ?: throw IllegalArgumentException("Primary key ${pkColumn.name} value can't be null")

            pkColumn.isAutoIncrement -> RocksAutoIncrementManager.getAndIncrement(
                database,
                cfHandle,
                table,
                pkColumn.name
            )

            else -> throw IllegalArgumentException("Primary key value for column ${pkColumn.name} is missing")
        }

        val key = pkColumn?.let {
            RocksRowKeyEncoder.encodePrimaryKey(table, pkValue!!)
        } ?: RocksRowKeyEncoder.encodeGeneratedRowKey()

        val rowValues = values.toMutableMap().apply {
            if (pkColumn != null) this[pkColumn.name] = pkValue
        }

        table.columns.forEach { (name, column) ->
            if (!column.isNullable && !rowValues.containsKey(name)) {
                throw IllegalArgumentException("Column $name cannot be null")
            }
        }

        val value = RocksRowValueCodec.forTable(table).encodeRow(rowValues as Map<String, ByteArray?>)

        database.database!!.put(cfHandle, key, value)

        return WriteResult(affectedCount = 1)
    }
}
