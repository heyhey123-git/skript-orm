package io.github.heyhey123.xiaojieorm.impl.rocksdb.storage

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.table.Table
import java.nio.ByteBuffer

/**
 * Persists auto-increment counters in the dedicated metadata column family.
 */
object RocksAutoIncrementManager {

    private const val AUTO_INCREMENT_PREFIX = "auto-increment:"

    /**
     * A monitor to prevent concurrent auto-increment updates.
     * When an update is in progress, no other updates can be started.
     */
    private val incrementLock = Any()

    /**
     * Get the current auto-increment value and increment it by 1.
     *
     * @param database The database instance
     * @param table The table
     * @param columnName The column
     * @return The next auto-increment value
     */
    fun getAndIncrement(
        database: RocksdbDatabase,
        table: Table,
        columnName: String
    ): Long = synchronized(incrementLock) {
        val current = getCurrent(database, table, columnName)
        val next = Math.addExact(current, 1L)
        set(database, table, columnName, next)
        next
    }

    /**
     * Get the current auto-increment value and increment it by the specified amount.
     *
     * @param database The database instance
     * @param table The table
     * @param columnName The column
     * @param count The amount
     * @return The next auto-increment value
     */
    fun getAndIncrementBatch(
        database: RocksdbDatabase,
        table: Table,
        columnName: String,
        count: Int
    ): LongRange = synchronized(incrementLock) {
        require(count >= 0) { "Auto-increment batch count cannot be negative." }
        val current = getCurrent(database, table, columnName)
        val next = Math.addExact(current, count.toLong())
        set(database, table, columnName, next)
        (current + 1)..next
    }

    /**
     * Get current auto-increment value, or 0 if not found.
     *
     * @param database The database instance
     * @param table The table
     * @param columnName The column
     * @return The current auto-increment value
     */
    fun getCurrent(
        database: RocksdbDatabase,
        table: Table,
        columnName: String
    ): Long {
        val bytes = database.database!!.get(
            database.metadataColumnFamilyHandle,
            encodeKey(table.name, columnName)
        ) ?: return 0L
        require(bytes.size == Long.SIZE_BYTES) {
            "Invalid auto-increment value for `${table.name}.$columnName`."
        }
        return ByteBuffer.wrap(bytes).long
    }

    /**
     * Set the current auto-increment value.
     *
     * @param database The database instance
     * @param table The table
     * @param columnName The column name
     * @param value The value to set
     */
    fun set(
        database: RocksdbDatabase,
        table: Table,
        columnName: String,
        value: Long
    ) {
        val bytes = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(value).array()
        database.database!!.put(
            database.metadataColumnFamilyHandle,
            encodeKey(table.name, columnName),
            bytes
        )
    }

    private fun encodeKey(tableName: String, columnName: String): ByteArray =
        "$AUTO_INCREMENT_PREFIX$tableName.$columnName".toByteArray(Charsets.UTF_8)
}
