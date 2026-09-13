package io.github.heyhey123.xiaojieorm.impl.rocksdb.result

import io.github.heyhey123.xiaojieorm.result.DataCursor
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter

/**
 * In-memory RocksDB query cursor.
 *
 * Row positioning follows JDBC semantics: the cursor starts before the first row
 * and [next] advances it. Numeric column access is one-based.
 */
class RocksDataCursor(
    val result: List<Map<String, Any?>>
) : DataCursor {

    private var currentIndex = -1

    override fun next() = ++currentIndex < result.size

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(column: String, dataType: DataType<T>): T? {
        val converter = dataType.converter as ValueConverter<T, Any>
        return result[currentIndex][column]?.let { converter.fromStorage(it) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(index: Int, dataType: DataType<T>): T? {
        require(index >= 1) { "Column index must be 1-based and positive." }
        val converter = dataType.converter as ValueConverter<T, Any>
        return result[currentIndex].values.elementAt(index - 1)?.let { converter.fromStorage(it) }
    }

    override fun close() {}
}
