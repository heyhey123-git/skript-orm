package io.github.heyhey123.xiaojieorm.impl.rocksdb.storage

import io.github.heyhey123.xiaojieorm.impl.rocksdb.database.RocksdbDatabase
import io.github.heyhey123.xiaojieorm.table.Table
import org.rocksdb.WriteBatch
import java.nio.ByteBuffer

/**
 * Persists auto-increment high-water marks in the dedicated metadata column family.
 */
object RocksAutoIncrementManager {

    private const val AUTO_INCREMENT_PREFIX = "auto-increment:"

    /**
     * Gets the current high-water mark for the given table and column. If the high-water mark is not set, returns 0.
     *
     * @param database the RocksDB database instance
     * @param table the table
     * @param columnName the column name
     * @return the current high-water mark, or 0 if not set
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
     * Returns the next sequence value without persisting it.
     *
     * @throws ArithmeticException if the sequence has reached [Long.MAX_VALUE]
     */
    fun nextAfter(current: Long): Long = Math.addExact(current, 1L)

    /**
     * Computes a monotonically increasing high-water mark.
     *
     * @return the greater of [current] and [candidate]
     */
    fun advanceToAtLeast(current: Long, candidate: Long): Long = maxOf(current, candidate)

    /**
     * Adds a counter update to the caller's atomic RocksDB write batch.
     * The update will be committed atomically with the rest of the write batch,
     * so that the high-water mark is only updated if the insert succeeds.
     * @param writeBatch the write batch to add the update to
     * @param database the RocksDB database instance
     * @param table the table
     * @param columnName the column name
     * @param value the new value for the counter
     */
    fun putCounter(
        writeBatch: WriteBatch,
        database: RocksdbDatabase,
        table: Table,
        columnName: String,
        value: Long
    ) {
        writeBatch.put(
            database.metadataColumnFamilyHandle,
            encodeKey(table.name, columnName),
            encodeValue(value)
        )
    }

    /**
     * Encode value for the auto-increment counter in the metadata column family.
     *
     * @param value the value to encode
     * @return the byte array representing the encoded value
     */
    private fun encodeValue(value: Long): ByteArray =
        ByteBuffer.allocate(Long.SIZE_BYTES).putLong(value).array()

    /**
     * Encode key for the auto-increment counter in the metadata column family.
     *
     * @param tableName the table name
     * @param columnName the column name
     * @return the encoded key to be used in the metadata column family
     */
    private fun encodeKey(tableName: String, columnName: String): ByteArray =
        "$AUTO_INCREMENT_PREFIX$tableName.$columnName".toByteArray(Charsets.UTF_8)
}
