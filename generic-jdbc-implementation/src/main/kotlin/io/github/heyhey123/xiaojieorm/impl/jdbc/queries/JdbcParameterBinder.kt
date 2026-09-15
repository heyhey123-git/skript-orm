package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import java.sql.Blob
import java.sql.PreparedStatement
import java.util.WeakHashMap

// Weak keys avoid retaining abandoned statements; synchronized access protects the shared registry.
private val boundBlobs = WeakHashMap<PreparedStatement, MutableList<Blob>>()

/**
 * Binds a value to a PreparedStatement at the specified index, converting it to the appropriate storage type.
 * Converted Blob values remain owned by the statement execution boundary and are freed afterwards.
 */
internal fun PreparedStatement.bindValue(index: Int, value: Any?, type: DataType<*>) {
    val jdbcType = requireNotNull(type as? JdbcDataType<*>) {
        "Data type ${type.typeCode} is not supported by the JDBC implementation."
    }
    if (value == null) {
        setNull(index, jdbcType.jdbcType.vendorTypeNumber)
        return
    }
    require(type.domainType.isInstance(value)) {
        "Value for type ${type.typeCode} must be ${type.domainType.name}, but was ${value.javaClass.name}."
    }
    @Suppress("UNCHECKED_CAST")
    val converter = type.converter as ValueConverter<Any, Any>
    val storageValue = converter.toStorage(value)
    try {
        setObject(index, storageValue, jdbcType.jdbcType)
        if (storageValue is Blob) {
            synchronized(boundBlobs) {
                boundBlobs.getOrPut(this) { mutableListOf() }.add(storageValue)
            }
        }
    } catch (error: Throwable) {
        if (storageValue is Blob) {
            try {
                storageValue.free()
            } catch (freeError: Throwable) {
                error.addSuppressed(freeError)
            }
        }
        throw error
    }
}

/**
 * Runs [action] and always releases temporary JDBC values bound to this statement.
 * Cleanup failures are suppressed onto an action failure; otherwise the cleanup failure is thrown.
 */
internal inline fun <T> PreparedStatement.withBoundResources(action: () -> T): T {
    var failure: Throwable? = null
    try {
        return action()
    } catch (error: Throwable) {
        failure = error
        throw error
    } finally {
        try {
            releaseBoundResources()
        } catch (cleanupError: Throwable) {
            if (failure != null) {
                failure.addSuppressed(cleanupError)
            } else {
                throw cleanupError
            }
        }
    }
}

/**
 * Frees temporary JDBC values owned by this statement.
 *
 * The first cleanup failure is thrown; later failures are attached as suppressed exceptions.
 */
internal fun PreparedStatement.releaseBoundResources() {
    val blobs = synchronized(boundBlobs) { boundBlobs.remove(this) }.orEmpty()
    var failure: Throwable? = null
    blobs.forEach { blob ->
        try {
            blob.free()
        } catch (error: Throwable) {
            if (failure == null) failure = error else failure.addSuppressed(error)
        }
    }
    failure?.let { throw it }
}
