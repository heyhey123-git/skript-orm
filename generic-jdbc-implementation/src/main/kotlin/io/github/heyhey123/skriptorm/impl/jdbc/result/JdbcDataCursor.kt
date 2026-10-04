package io.github.heyhey123.skriptorm.impl.jdbc.result

import io.github.heyhey123.skriptorm.impl.jdbc.type.consumeBytes
import io.github.heyhey123.skriptorm.result.DataCursor
import io.github.heyhey123.skriptorm.type.ConversionThread
import io.github.heyhey123.skriptorm.type.DataType
import io.github.heyhey123.skriptorm.type.DeferredDatabaseValue
import io.github.heyhey123.skriptorm.type.ValueConverter
import java.sql.Blob
import java.sql.ResultSet
import java.sql.SQLFeatureNotSupportedException
import java.sql.Statement
import javax.sql.rowset.serial.SerialBlob

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

    override fun getDetached(column: String, dataType: DataType<*>): Any? =
        detach(readStorage(column, dataType.converter.storageType), dataType)

    override fun getDetached(index: Int, dataType: DataType<*>): Any? =
        detach(readStorage(index, dataType.converter.storageType), dataType)

    @Suppress("UNCHECKED_CAST")
    private fun detach(storage: Any?, type: DataType<*>): Any? {
        if (resultSet.wasNull() || storage == null) return null
        val converter = type.converter as ValueConverter<Any, Any>
        if (converter.readThread == ConversionThread.ANY) return converter.fromStorage(storage)
        // Consume JDBC-owned bytes while the cursor is open; no driver resource crosses the boundary.
        val snapshot = when (storage) {
            is Blob -> storage.consumeBytes { it }
            is ByteArray -> storage.copyOf()
            is java.sql.Timestamp -> java.sql.Timestamp(storage.time).also { it.nanos = storage.nanos }
            is java.sql.Time -> java.sql.Time(storage.time)
            is java.sql.Date -> java.sql.Date(storage.time)
            is java.sql.Clob, is java.sql.SQLXML, is java.sql.Array, is java.sql.Ref,
            is java.io.InputStream, is java.io.Reader -> throw IllegalArgumentException(
                "Storage type ${converter.storageType.name} cannot be detached from a JDBC cursor. " +
                    "Use byte-array or Blob storage for a converter that requires the server thread."
            )
            else -> storage
        }
        return DeferredDatabaseValue(converter.readThread) {
            if (converter.storageType == Blob::class.java) {
                val blob = SerialBlob(snapshot as ByteArray)
                var failure: Throwable? = null
                try {
                    converter.fromStorage(blob)
                } catch (error: Throwable) {
                    failure = error
                    throw error
                } finally {
                    // Built-in converters already free the blob. SerialBlob.free() is idempotent.
                    try {
                        blob.free()
                    } catch (error: Throwable) {
                        if (failure != null) failure.addSuppressed(error) else throw error
                    }
                }
            } else {
                converter.fromStorage(snapshot)
            }
        }
    }

    /**
     * Reads one column in the storage class its converter expects.
     *
     * A `Blob` is read with the dedicated accessor, because Connector/J rejects a typed `getObject`
     * for it. Without that, every row carrying a Blob-backed column is unreadable, including the rows
     * where that column is SQL NULL, since the driver rejects the requested conversion before it
     * looks at the value. Every other storage type uses the JDBC 4.2 typed read.
     *
     * A SQL NULL is found before that read is made, because a driver may reject the requested
     * conversion for a value that is not there at all: SQLite's does exactly that for an `INTEGER`
     * column, so a row whose nullable number was never written could not be read back. The untyped
     * read is the one call that reports absence rather than a conversion.
     */
    private fun readStorage(column: String, storageType: Class<*>): Any? {
        if (storageType == Blob::class.java) {
            return try {
                resultSet.getBlob(column)
            } catch (_: SQLFeatureNotSupportedException) {
                resultSet.getBytes(column)?.let { SerialBlob(it) }
            }
        }
        if (storageType == ByteArray::class.java) return resultSet.getBytes(column)
        if (resultSet.getObject(column) == null) return null
        return resultSet.getObject(column, boxed(storageType))
    }

    private fun readStorage(index: Int, storageType: Class<*>): Any? {
        if (storageType == Blob::class.java) {
            return try {
                resultSet.getBlob(index)
            } catch (_: SQLFeatureNotSupportedException) {
                resultSet.getBytes(index)?.let { SerialBlob(it) }
            }
        }
        if (storageType == ByteArray::class.java) return resultSet.getBytes(index)
        if (resultSet.getObject(index) == null) return null
        return resultSet.getObject(index, boxed(storageType))
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
            releaseConnection()
        } catch (error: Throwable) {
            if (failure == null) failure = error else failure.addSuppressed(error)
        }
        failure?.let { throw it }
    }
}
