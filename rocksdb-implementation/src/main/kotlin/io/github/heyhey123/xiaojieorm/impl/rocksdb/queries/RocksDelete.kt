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

class RocksDelete(
    limit: Int?,
    where: WhereClause?,
    override val database: RocksdbDatabase
) : Delete(limit, where), RocksQuery {

    override suspend fun execute(table: Table): WriteResult =
        database.withMutationLock {
            val deleteLimit = limit
            require(deleteLimit == null || deleteLimit >= 0) { "Delete limit cannot be negative." }
            if (deleteLimit == 0) return@withMutationLock WriteResult(0)

            val db = requireNotNull(database.database) { "Database is not connected" }
            val columnFamily = requireNotNull(database.columnFamilyHandles[table.name]) {
                "Column family for table `${table.name}` not found"
            }
            val keys = when (val translated = RocksConditionTranslator.translate(where, table)) {
                is TranslateResult.PrimaryKeyLookup -> {
                    val key = RocksRowKeyEncoder.encodePrimaryKey(table, translated.pkValue)
                    if (db.keyExists(columnFamily, key)) listOf(key) else emptyList()
                }

                is TranslateResult.ScanWithFilter -> {
                    val result = mutableListOf<ByteArray>()
                    val codec = RocksRowValueCodec.forTable(table)
                    db.newIterator(columnFamily).use { iterator ->
                        iterator.seekToFirst()
                        while (iterator.isValid && (deleteLimit == null || result.size < deleteLimit)) {
                            if (translated.predicate(codec.decodeRow(iterator.value()))) {
                                result += iterator.key()
                            }
                            iterator.next()
                        }
                    }
                    result
                }
            }

            if (keys.isEmpty()) return@withMutationLock WriteResult(0)
            WriteBatch().use { batch ->
                keys.forEach { batch.delete(columnFamily, it) }
                WriteOptions().use { options -> db.write(options, batch) }
            }
            WriteResult(keys.size.toLong())
        }
}
