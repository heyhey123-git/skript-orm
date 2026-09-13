package io.github.heyhey123.xiaojieorm.impl.rocksdb.queries

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksAutoIncrementManager
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowKeyEncoder
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.RocksRowValueCodec
import io.github.heyhey123.xiaojieorm.queries.InsertMany
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import org.rocksdb.ColumnFamilyHandle
import org.rocksdb.RocksDB
import org.rocksdb.WriteBatch
import org.rocksdb.WriteOptions

/** Atomically inserts a batch of rows without overwriting existing primary keys. */
class RocksInsertMany(
    valuesList: List<Map<String, Any?>>,
    override val database: RocksdbDatabase
) : InsertMany(valuesList), RocksQuery {

    override suspend fun execute(table: Table): WriteResult {
        if (valuesList.isEmpty()) return WriteResult(0)
        return database.withMutationLock { mutateLocked(table) }
    }

    /** Executes the insert while the database insert lock is held. */
    private fun mutateLocked(table: Table): WriteResult {
        val db = requireNotNull(database.database) { "Database is not connected" }
        val columnFamily = requireNotNull(database.columnFamilyHandles[table.name]) {
            "Column family for table `${table.name}` not found"
        }
        val plan = buildInsertPlan(db, columnFamily, table)

        WriteBatch().use { batch ->
            plan.rows.forEach { row ->
                batch.put(columnFamily, row.key, row.value)
            }
            addHighWaterMark(batch, table, plan.highWaterMark)
            WriteOptions().use { options -> db.write(options, batch) }
        }
        return WriteResult(plan.rows.size)
    }

    /** Resolves and validates every row before anything is written. */
    private fun buildInsertPlan(
        db: RocksDB,
        columnFamily: ColumnFamilyHandle,
        table: Table
    ): InsertPlan {
        var highWaterMark = readHighWaterMark(table)
        val rows = ArrayList<EncodedRow>(valuesList.size)
        val batchKeys = HashSet<ByteArrayKey>(valuesList.size)
        val codec = RocksRowValueCodec.forTable(table)

        valuesList.forEach { values ->
            val primaryKey = RocksInsertPrimaryKeyResolver.resolve(
                values,
                table,
                highWaterMark
            )
            highWaterMark = primaryKey.highWaterMark

            val rowValues = buildRowValues(values, table, primaryKey.value)
            val key = encodeKey(table, primaryKey.value)
            ensurePrimaryKeyAvailable(db, columnFamily, table, key, batchKeys)
            rows += EncodedRow(key, codec.encodeRow(rowValues))
        }
        return InsertPlan(rows, highWaterMark)
    }

    private fun readHighWaterMark(table: Table): Long? =
        table.primaryKey
            ?.takeIf { it.isAutoIncrement }
            ?.let { RocksAutoIncrementManager.getCurrent(database, table, it.name) }

    private fun buildRowValues(
        sourceValues: Map<String, Any?>,
        table: Table,
        primaryKeyValue: Any?
    ): Map<String, Any?> = sourceValues.toMutableMap().apply {
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

    private fun ensurePrimaryKeyAvailable(
        db: RocksDB,
        columnFamily: ColumnFamilyHandle,
        table: Table,
        key: ByteArray,
        batchKeys: MutableSet<ByteArrayKey>
    ) {
        check(batchKeys.add(ByteArrayKey(key))) {
            "Duplicate primary key in insert batch for table `${table.name}`"
        }
        check(!db.keyExists(columnFamily, key)) {
            "Duplicate primary key for table `${table.name}`"
        }
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

    private data class EncodedRow(
        val key: ByteArray,
        val value: ByteArray
    )

    private data class InsertPlan(
        val rows: List<EncodedRow>,
        val highWaterMark: Long?
    )

    private class ByteArrayKey(private val bytes: ByteArray) {
        override fun equals(other: Any?): Boolean =
            other is ByteArrayKey && bytes.contentEquals(other.bytes)

        override fun hashCode(): Int = bytes.contentHashCode()
    }
}
