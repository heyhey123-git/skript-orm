package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksAutoIncrementManager
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.InsertOne
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.rocksdb.WriteBatch
import org.rocksdb.WriteOptions

/**
 * Inserts one row without overwriting an existing primary key.
 *
 * @property values the values to insert
 * @property database the database to insert into
 */
class RocksInsertOne(
    values: Map<String, Any?>,
    override val database: RocksdbDatabase
) : InsertOne(values), RocksQuery {

    override suspend fun execute(table: Table): WriteResult =
        database.withMutationLock { mutateLocked(table) }

    /** Executes the insert while the database insert lock is held. */
    private fun mutateLocked(table: Table): WriteResult {
        val db = requireNotNull(database.database) { "Database is not connected" }
        val columnFamily = requireNotNull(database.columnFamilyHandles[table.name]) {
            "Column family for table `${table.name}` not found"
        }
        val primaryKey = RocksInsertPrimaryKeyResolver.resolve(
            values,
            table,
            readHighWaterMark(table)
        )
        val rowValues = buildRowValues(table, primaryKey.value)
        val key = encodeKey(table, primaryKey.value)
        val encodedValue = RocksRowValueCodec.forTable(table).encodeRow(rowValues)

        check(!db.keyExists(columnFamily, key)) {
            "Duplicate primary key for table `${table.name}`"
        }

        WriteBatch().use { batch ->
            batch.put(columnFamily, key, encodedValue)
            addHighWaterMark(batch, table, primaryKey.highWaterMark)
            WriteOptions().use { options -> db.write(options, batch) }
        }
        return WriteResult(affectedCount = 1)
    }

    private fun readHighWaterMark(table: Table): Long? =
        table.primaryKey
            ?.takeIf { it.isAutoIncrement }
            ?.let { RocksAutoIncrementManager.getCurrent(database, table, it.name) }

    private fun buildRowValues(table: Table, primaryKeyValue: Any?): Map<String, Any?> =
        values.toMutableMap().apply {
            table.primaryKey?.let { this[it.name] = primaryKeyValue }
            table.columns.forEach { (name, column) ->
                require(column.isNullable || this[name] != null) {
                    "Column $name cannot be null"
                }
            }
        }

    private fun encodeKey(table: Table, primaryKeyValue: Any?): ByteArray =
        if (table.primaryKey == null) {
            RocksRowKeyEncoder.encodeGeneratedRowKey()
        } else {
            RocksRowKeyEncoder.encodePrimaryKey(table, requireNotNull(primaryKeyValue))
        }

    private fun addHighWaterMark(
        batch: WriteBatch,
        table: Table,
        highWaterMark: Long?
    ) {
        val column = table.primaryKey?.takeIf { it.isAutoIncrement } ?: return
        RocksAutoIncrementManager.putCounter(
            batch,
            database,
            table,
            column.name,
            requireNotNull(highWaterMark)
        )
    }
}
