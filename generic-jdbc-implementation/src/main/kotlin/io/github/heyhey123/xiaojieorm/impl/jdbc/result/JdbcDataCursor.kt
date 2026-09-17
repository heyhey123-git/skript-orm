package io.github.heyhey123.xiaojieorm.impl.jdbc.result

import io.github.heyhey123.xiaojieorm.result.DataCursor
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import java.sql.Blob
import java.sql.ResultSet
import java.sql.Statement

/**
 * JDBC cursor that owns its bound parameter resources, its result set, and its statement, and that
 * hands its connection back through [releaseConnection].
 *
 * The connection is not the cursor's to close. Outside a transaction [releaseConnection] returns it to
 * the pool; inside one it does nothing, because the transaction owns that connection and closing it
 * here would end the transaction from underneath the script.
 *
 * [close] is idempotent and releases those resources in ownership order. If cleanup steps fail,
 * the first failure is thrown and later failures are attached as suppressed exceptions. Values are
 * read with the JDBC 4.2 typed `getObject`, except for `Blob` storage, which has its own accessor;
 * SQL NULL is returned as Kotlin `null` before conversion.
 */
class JdbcDataCursor(
    private val resultSet: ResultSet,
    private val statement: Statement,
    private val releaseConnection: () -> Unit,
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
        val storageValue = readStorage(column, converter.storageType)
        if (resultSet.wasNull() || storageValue == null) return null
        return converter.fromStorage(storageValue)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(index: Int, dataType: DataType<T>): T? {
        val converter = dataType.converter as ValueConverter<T, Any>
        val storageValue = readStorage(index, converter.storageType)
        if (resultSet.wasNull() || storageValue == null) return null
        return converter.fromStorage(storageValue)
    }

    /**
     * Reads one column in the storage class its converter expects.
     *
     * A `Blob` is read with the dedicated accessor, because Connector/J rejects a typed `getObject`
     * for it. Without that, every row carrying a Blob-backed column is unreadable, including the rows
     * where that column is SQL NULL, since the driver rejects the requested conversion before it
     * looks at the value. Every other storage type uses the JDBC 4.2 typed read.
     */
    private fun readStorage(column: String, storageType: Class<*>): Any? =
        if (storageType == Blob::class.java) resultSet.getBlob(column) else resultSet.getObject(column, boxed(storageType))

    private fun readStorage(index: Int, storageType: Class<*>): Any? =
        if (storageType == Blob::class.java) resultSet.getBlob(index) else resultSet.getObject(index, boxed(storageType))

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
            releaseConnection()
        } catch (error: Throwable) {
            if (failure == null) failure = error else failure.addSuppressed(error)
        }
        failure?.let { throw it }
    }
}
