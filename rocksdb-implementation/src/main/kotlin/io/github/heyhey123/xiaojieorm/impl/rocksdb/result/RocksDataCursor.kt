package io.github.heyhey123.xiaojieorm.impl.rocksdb.result

import io.github.heyhey123.xiaojieorm.result.DataCursor
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter

class RocksDataCursor(
    val result: List<Map<String, Any?>>
) : DataCursor {

    var currentIndex = -1

    override fun next() = currentIndex++ < result.size

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(column: String, dataType: DataType<T>): T? {
        val converter = dataType.converter as ValueConverter<T, Any>
        return result[currentIndex][column]?.let { converter.fromStorage(it) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(index: Int, dataType: DataType<T>): T? {
        val converter = dataType.converter as ValueConverter<T, Any>
        return result[currentIndex].values.elementAt(index)?.let { converter.fromStorage(it) }
    }

    override fun close() {}
}
