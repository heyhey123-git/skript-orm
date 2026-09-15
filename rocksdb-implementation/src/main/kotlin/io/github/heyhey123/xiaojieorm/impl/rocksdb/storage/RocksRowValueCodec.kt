package io.github.heyhey123.xiaojieorm.impl.rocksdb.storage

import io.github.heyhey123.xiaojieorm.impl.rocksdb.type.RocksDataType
import io.github.heyhey123.xiaojieorm.table.Table
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

/**
 * Codec for encoding and decoding rows in RocksDB.
 *
 * @property table The table to encode/decode rows for
 */
class RocksRowValueCodec private constructor(
    private val table: Table
) {

    companion object {

        /**
         * Cache of codecs for tables.
         */
        val cache: MutableMap<Table, RocksRowValueCodec> = ConcurrentHashMap()

        /**
         * Get or create a codec for the given table.
         *
         * @param table The table to get the codec for
         * @return The codec for the table
         */
        fun forTable(table: Table): RocksRowValueCodec =
            cache.computeIfAbsent(table, ::RocksRowValueCodec)
    }

    /**
     * Encode the as the following format:
     * * [column count: short]
     * * for each column:
     *  * [column name length: short]
     *  * [column name: bytes]
     *  * [value length: int] (-1 means null)
     *  * [value: bytes]
     *
     * @param values The map of column names to their values.
     * @return The encoded byte array.
     */
    @Suppress("UNCHECKED_CAST")
    fun encodeRow(values: Map<String, Any?>): ByteArray {
        val columns = table.columns

        /**
         * Encoded column.
         *
         */
        data class EncodedCol(
            val nameBytes: ByteArray,
            val valueBytes: ByteArray?
        )

        val encoded = ArrayList<EncodedCol>(columns.size)
        var totalLen = 2 // column count length

        for ((name, column) in columns) {
            val nameBytes = name.toByteArray(Charsets.UTF_8)

            totalLen += 2 + nameBytes.size + 4 // the total length of this column

            val value = values[name]
            if (value == null) {
                encoded += EncodedCol(nameBytes, null)
                continue
            }

            val valueBytes = if (value is ByteArray) {
                value
            } else {
                val dataType = column.type as? RocksDataType<Any>
                    ?: throw IllegalArgumentException(
                        "Column `$name` in table `${table.name}` is not backed by a Rocks data type"
                    )
                dataType.converter.toStorage(value)
            }

            totalLen += valueBytes.size
            encoded += EncodedCol(nameBytes, valueBytes)
        }

        val buffer = ByteBuffer.allocate(if (totalLen > 0) totalLen else 2)

        // column count
        buffer.putShort(columns.size.toShort())

        for (col in encoded) {
            // column name length + name bytes
            buffer.putShort(col.nameBytes.size.toShort())
            buffer.put(col.nameBytes)

            val valueBytes = col.valueBytes
            if (valueBytes == null) {
                // null -> value length = -1
                buffer.putInt(-1)
            } else {
                // value length + value bytes
                buffer.putInt(valueBytes.size)
                buffer.put(valueBytes)
            }
        }

        return buffer.array()
    }

    /**
     * Decode row from byte array.
     * Follows the format defined in [encodeRow].
     *
     * @param bytes The byte array to decode.
     * @return The decoded row as a map of column names to their unconverted values.
     */
    fun decodeRow(bytes: ByteArray): Map<String, ByteArray?> {
        val result = mutableMapOf<String, ByteArray?>()
        val buffer = ByteBuffer.wrap(bytes)

        val columnCount = buffer.short.toInt()

        repeat(columnCount) {
            val nameLen = buffer.short.toInt()
            val nameBytes = ByteArray(nameLen)
            buffer.get(nameBytes)
            val columnName = String(nameBytes, Charsets.UTF_8)

            val valueLen = buffer.int
            if (valueLen == -1) {
                result[columnName] = null
                return@repeat
            }

            val valueBytes = ByteArray(valueLen)
            buffer.get(valueBytes)

            result[columnName] = valueBytes
        }

        return result
    }
}
