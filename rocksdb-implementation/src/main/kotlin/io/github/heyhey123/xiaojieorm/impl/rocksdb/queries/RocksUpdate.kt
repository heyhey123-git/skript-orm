package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.rocksdb.condition.RocksConditionTranslator
import io.github.heyhey123.xiaojieorm.impl.rocksdb.condition.TranslateResult
import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowMutation
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

    override suspend fun execute(table: Table): WriteResult =
        database.withMutationLock { updateLocked(table) }

    private fun updateLocked(table: Table): WriteResult {
        val updateLimit = limit
        require(updateLimit == null || updateLimit >= 0) { "Update limit cannot be negative." }
        if (updateLimit == 0 || values.isEmpty()) return WriteResult(0)
        RocksRowMutation.requireKnownColumns(table, values)
        requirePrimaryKeyUnchanged(table)

        val db = requireNotNull(database.database) { "Database is not connected" }
        val columnFamily = requireNotNull(database.columnFamilyHandles[table.name]) {
            "Column family for table `${table.name}` not found"
        }
        val codec = RocksRowValueCodec.forTable(table)

        val updates = when (val translated = RocksConditionTranslator.translate(where, table)) {
            is TranslateResult.PrimaryKeyLookup -> {
                val key = RocksRowKeyEncoder.encodePrimaryKey(table, translated.pkValue)
                val stored = db.get(columnFamily, key) ?: return WriteResult(0)
                listOf(key to codec.encodeRow(RocksRowMutation.merge(table, codec.decodeRow(stored), values)))
            }

            is TranslateResult.ScanWithFilter -> {
                val result = mutableListOf<Pair<ByteArray, ByteArray>>()
                db.newIterator(columnFamily).use { iterator ->
                    iterator.seekToFirst()
                    while (iterator.isValid && (updateLimit == null || result.size < updateLimit)) {
                        val row = codec.decodeRow(iterator.value())
                        if (translated.predicate(row)) {
                            val merged = RocksRowMutation.merge(table, row, values)
                            result += iterator.key() to codec.encodeRow(merged)
                        }
                        iterator.next()
                    }
                }
                result
            }
        }

        if (updates.isEmpty()) return WriteResult(0)
        WriteBatch().use { batch ->
            updates.forEach { (key, value) -> batch.put(columnFamily, key, value) }
            WriteOptions().use { options -> db.write(options, batch) }
        }
        return WriteResult(updates.size.toLong())
    }

    private fun requirePrimaryKeyUnchanged(table: Table) {
        val primaryKey = table.primaryKey ?: return
        require(primaryKey.name !in values) {
            "Updating primary key column `${primaryKey.name}` is not supported."
        }
    }
}
