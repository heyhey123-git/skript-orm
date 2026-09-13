package io.github.heyhey123.xiaojieorm.impl.jdbc.result

import io.github.heyhey123.xiaojieorm.result.DataCursor
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class JdbcDataCursor(
    private val resultSet: ResultSet,
    private val statement: Statement,
    private val connection: Connection
) : DataCursor {
    private var closed = false

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
        if (closed) return
        closed = true
        var failure: Throwable? = null
        try { resultSet.close() } catch (error: Throwable) { failure = error }
        try { statement.close() } catch (error: Throwable) {
            if (failure == null) failure = error else failure.addSuppressed(error)
        }
        try { connection.close() } catch (error: Throwable) {
            if (failure == null) failure = error else failure.addSuppressed(error)
        }
        failure?.let { throw it }
    }
}
