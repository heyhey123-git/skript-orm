package io.github.heyhey123.xiaojieorm.impl.rocksdb.storage

import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.EncoderUtils.PRIMARY_KEY_MARKER
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.EncoderUtils.ROW_MARKER
import io.github.heyhey123.xiaojieorm.impl.rocksdb.type.RocksDataType
import io.github.heyhey123.xiaojieorm.impl.rocksdb.type.StringRocksConverter
import io.github.heyhey123.xiaojieorm.table.Table
import java.util.concurrent.ConcurrentHashMap

/**
 * Rocks row key encoder.
 *
 * @property table The table to encode keys for
 * @constructor Create empty Rocks row key encoder
 */
object RocksRowKeyEncoder {

    /**
     * Cache of encoded primary keys.
     */
    val encodedPrimaryKeys: MutableMap<Table, ByteArray> = ConcurrentHashMap()

    /**
     * Encode primary key.
     *
     * @param table The table
     * @param id The primary key value
     * @return Encoded primary key as byte array
     */
    fun encodePrimaryKey(table: Table, id: Any): ByteArray {
        if (table in encodedPrimaryKeys) {
            return encodedPrimaryKeys[table]!!
        }

        val pkColumn = table.primaryKey
            ?: error("Table `${table.name}` has no primary key")
        val dataType = pkColumn.type

        val converter = (dataType as RocksDataType<Any>).converter
        val pkBytes = converter.toStorage(id)
        val result = byteArrayOf(PRIMARY_KEY_MARKER) + pkBytes

        encodedPrimaryKeys[table] = result
        return result
    }

    /**
     * Encode generated row key, used for rows without primary key.
     *
     * @return Encoded generated row key as byte array
     */
    fun encodeGeneratedRowKey(): ByteArray {
        val rowId = EncoderUtils.generateRowId()
        val rowIdBytes = StringRocksConverter
            .toStorage(rowId)
        return byteArrayOf(ROW_MARKER) + rowIdBytes
    }
}
