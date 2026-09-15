package io.github.heyhey123.xiaojieorm.impl.jdbc.result

import io.github.heyhey123.xiaojieorm.result.DataCursor
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

/**
 * JDBC cursor that owns its bound parameter resources, result set, statement, and connection.
 *
 * [close] is idempotent and releases those resources in ownership order. If cleanup steps fail,
 * the first failure is thrown and later failures are attached as suppressed exceptions. Values are
 * read with JDBC 4.2 typed `getObject`; SQL NULL is returned as Kotlin `null` before conversion.
 */
class JdbcDataCursor(
    private val resultSet: ResultSet,
    private val statement: Statement,
    private val connection: Connection,
    private val releaseBoundResources: (() -> Unit)? = null
) : DataCursor {

    private var closed = false

    override fun next(): Boolean = resultSet.next()

    private fun boxed(type: Class<*>): Class<*> = when (type) {
        java.lang.Boolean.TYPE -> java.lang.Boolean::class.java
        java.lang.Byte.TYPE -> java.lang.Byte::class.java
        java.lang.Short.TYPE -> java.lang.Short::class.java
        java.lang.Integer.TYPE -> java.lang.Integer::class.java
        java.lang.Long.TYPE -> java.lang.Long::class.java
        java.lang.Float.TYPE -> java.lang.Float::class.java
        java.lang.Double.TYPE -> java.lang.Double::class.java
        java.lang.Character.TYPE -> java.lang.Character::class.java
        else -> type
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(column: String, dataType: DataType<T>): T? {
        val converter = dataType.converter as ValueConverter<T, Any>
        val storageValue = resultSet.getObject(column, boxed(converter.storageType))
        if (resultSet.wasNull()) return null
        return converter.fromStorage(storageValue)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(index: Int, dataType: DataType<T>): T? {
        val converter = dataType.converter as ValueConverter<T, Any>
        val storageValue = resultSet.getObject(index, boxed(converter.storageType))
        if (resultSet.wasNull()) return null
        return converter.fromStorage(storageValue)
    }

    override fun close() {
        if (closed) return
        closed = true
        var failure: Throwable? = null
        try {
            releaseBoundResources?.invoke()
        } catch (error: Throwable) {
            failure = error
        }
        try {
            resultSet.close()
        } catch (error: Throwable) {
            if (failure == null) failure = error else failure.addSuppressed(error)
        }
        try {
            statement.close()
        } catch (error: Throwable) {
            if (failure == null) failure = error else failure.addSuppressed(error)
        }
        try {
            connection.close()
        } catch (error: Throwable) {
            if (failure == null) failure = error else failure.addSuppressed(error)
        }
        failure?.let { throw it }
    }
}
