package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.rocksdb.condition.RocksConditionTranslator
import io.github.heyhey123.xiaojieorm.impl.rocksdb.condition.TranslateResult
import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.Delete
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.rocksdb.WriteBatch
import org.rocksdb.WriteOptions
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.use

class RocksDelete(
    limit: Int?,
    where: WhereClause?,
    override val database: RocksdbDatabase
): Delete(limit, where), RocksQuery {
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

                db.delete(cfHandle, key)
                return WriteResult(affectedCount = 1)
            }

            is TranslateResult.ScanWithFilter -> {
                val deletedRows = mutableListOf<ByteArray>()

                db.newIterator(cfHandle).use { iterator ->
                    iterator.seekToFirst()
                    while (iterator.isValid) {
                        if (limit != null && deletedRows.size >= limit!!) {
                            break
                        }

                        val row = codec.decodeRow(iterator.value())
                        if (!conditions.predicate(row)) {
                            iterator.next()
                            continue
                        }

                        deletedRows.add(iterator.key())

                        iterator.next()
                    }
                }

                WriteBatch().use { writeBatch ->
                    WriteOptions().use { writeOptions ->
                        for (key in deletedRows) {
                            writeBatch.delete(cfHandle, key)
                        }
                        db.write(writeOptions, writeBatch)
                    }
                }

                return WriteResult(deletedRows.size)
            }
        }
    }
}
