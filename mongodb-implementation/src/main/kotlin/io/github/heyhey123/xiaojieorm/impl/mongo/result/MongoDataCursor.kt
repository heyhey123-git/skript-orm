package io.github.heyhey123.xiaojieorm.impl.mongo.result

import io.github.heyhey123.xiaojieorm.result.DataCursor
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import org.bson.Document

class MongoDataCursor(
    val result: List<Document>
) : DataCursor {

    private var currentIndex = -1

    override fun next(): Boolean = currentIndex++ < result.size

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(column: String, dataType: DataType<T>): T? {
        val storageValue = result[currentIndex][column] ?: return null
        return (dataType.converter as ValueConverter<T, Any>).fromStorage(storageValue!!)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(index: Int, dataType: DataType<T>): T? {
        val storageValue = result[currentIndex].values.elementAt(index) ?: return null
        return (dataType.converter as ValueConverter<T, Any>).fromStorage(storageValue!!)
    }

    override fun close() {}
}
