package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.rocksdb.condition.RocksConditionTranslator
import io.github.heyhey123.xiaojieorm.impl.rocksdb.condition.TranslateResult
import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.Update
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.rocksdb.WriteBatch
import org.rocksdb.WriteOptions

class RocksUpdate(
    values: Map<String, Any?>,
    limit: Int?,
    where: WhereClause?,
    override val database: RocksdbDatabase
) : Update(values, limit, where), RocksQuery {
    override suspend fun execute(table: Table): WriteResult {
        val db = database.database!!
        val conditions = RocksConditionTranslator.translate(where, table)
        val cfHandle = database.columnFamilyHandles[table.name]
            ?: throw IllegalStateException("Column family for table ${table.name} not found")
        val codec = RocksRowValueCodec.forTable(table)

        when (conditions) {
            is TranslateResult.PrimaryKeyLookup -> {
                val key = RocksRowKeyEncoder.encodePrimaryKey(table, conditions.pkValue)
                val valueExists = db.keyExists(cfHandle, key)
                if (!valueExists) {
                    return WriteResult(affectedCount = 0)
                }

                val value = codec.encodeRow(values)

                db.put(cfHandle, key, value)
                return WriteResult(affectedCount = 1)
            }

            is TranslateResult.ScanWithFilter -> {

                val updates = mutableMapOf<ByteArray, ByteArray>()

                db.newIterator(cfHandle).use { iterator ->
                    iterator.seekToFirst()
                    while (iterator.isValid) {
                        if (limit != null && updates.size >= limit!!) {
                            break
                        }

                        val row = codec.decodeRow(iterator.value())
                        if (!conditions.predicate(row)) {
                            iterator.next()
                            continue
                        }

                        val key = iterator.key()
                        val newValue = codec.encodeRow(values)

                        updates[key] = newValue
                        iterator.next()
                    }
                }

                WriteBatch().use { writeBatch ->
                    WriteOptions().use { writeOptions ->
                        for ((key, value) in updates) {
                            writeBatch.put(cfHandle, key, value)
                        }
                        db.write(writeOptions, writeBatch)
                    }
                }

                return WriteResult(updates.size)
            }
        }
    }
}
