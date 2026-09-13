package io.github.heyhey123.xiaojieorm.impl.rocksdb.storage

import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.EncoderUtils.PRIMARY_KEY_MARKER
import io.github.heyhey123.xiaojieorm.impl.rocksdb.storage.EncoderUtils.ROW_MARKER
import io.github.heyhey123.xiaojieorm.impl.rocksdb.type.RocksDataType
import io.github.heyhey123.xiaojieorm.impl.rocksdb.type.StringRocksConverter
import io.github.heyhey123.xiaojieorm.table.Table

/**
 * Encoder for row keys.
 *
 */
object RocksRowKeyEncoder {

    /**
     * Encode primary key.
     *
     * The public query API may supply either a domain value or an already converted
     * storage value, so ByteArray values are used directly.
     *
     * @param table The table
     * @param id The primary key value
     * @return Encoded primary key as byte array
     */
    @Suppress("UNCHECKED_CAST")
    fun encodePrimaryKey(table: Table, id: Any): ByteArray {
        val pkColumn = table.primaryKey
            ?: error("Table `${table.name}` has no primary key")
        val converter = (pkColumn.type as RocksDataType<Any>).converter
        val pkBytes = converter.toStorage(id)
        return byteArrayOf(PRIMARY_KEY_MARKER) + pkBytes
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
