package io.github.heyhey123.xiaojieorm.impl.jdbc.result

import io.github.heyhey123.xiaojieorm.result.DataCursor
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import java.sql.ResultSet

/**
 * A JDBC implementation of the DataCursor interface.
 *
 * @property resultSet The JDBC ResultSet to iterate over.
 */
class JdbcDataCursor(
    val resultSet: ResultSet
) : DataCursor {

    override fun next(): Boolean = resultSet.next()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(column: String, dataType: DataType<T>): T? {
        val storageValue = resultSet.getObject(column, dataType.converter.storageType)
        if (resultSet.wasNull()) return null
        return (dataType.converter as ValueConverter<T, Any>).fromStorage(storageValue)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(index: Int, dataType: DataType<T>): T? {
        val storageValue = resultSet.getObject(index, dataType.converter.storageType)
        if (resultSet.wasNull()) return null
        return (dataType.converter as ValueConverter<T, Any>).fromStorage(storageValue)
    }

    override fun close() {
        resultSet.close()
    }
}
