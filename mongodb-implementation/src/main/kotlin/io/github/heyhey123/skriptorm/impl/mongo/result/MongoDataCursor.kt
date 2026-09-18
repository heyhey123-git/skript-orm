package io.github.heyhey123.skriptorm.impl.mongo.result

import io.github.heyhey123.skriptorm.result.DataCursor
import io.github.heyhey123.skriptorm.type.DataType
import io.github.heyhey123.skriptorm.type.ValueConverter
import org.bson.Document

/**
 * Cursor over the documents a select already read.
 *
 * MongoDB returns a document as a map of field names, so a value is looked up by name and a field the
 * document does not carry reads as null, exactly like the SQL NULL of a column a row left unset. The
 * index overload needs an order a map does not have: it walks [columns], the table's declaration order,
 * which is the order a `SELECT *` returns the same row in.
 *
 * The whole result set is materialized before execution returns, so there is no server cursor left to
 * close and [close] has nothing to release. A document mutated after the read is not seen here.
 */
class MongoDataCursor(
    private val result: List<Document>,
    private val columns: List<String>
) : DataCursor {

    private var currentIndex = -1

    override fun next(): Boolean = ++currentIndex < result.size

    override fun <T : Any> get(column: String, dataType: DataType<T>): T? =
        convert(result[currentIndex][column], dataType)

    override fun <T : Any> get(index: Int, dataType: DataType<T>): T? =
        convert(result[currentIndex][columns[index - 1]], dataType)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> convert(storageValue: Any?, dataType: DataType<T>): T? {
        if (storageValue == null) return null
        return (dataType.converter as ValueConverter<T, Any>).fromStorage(storageValue)
    }

    override fun close() {}
}
