package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import java.sql.PreparedStatement

/**
 * Binds a value to a PreparedStatement at the specified index, converting it to the appropriate storage type based on the provided DataType.
 * Finally, the value will be transformed to the storage type using the converter defined in the DataType.
 *
 * @param index The 1-based index of the parameter to bind in the PreparedStatement.
 * @param value The value to bind to the PreparedStatement. Can be null.
 * @param type The DataType that describes how to convert the value to the appropriate storage type for the database.
 * @throws IllegalArgumentException if the provided DataType is not a JdbcDataType,
 * or if the value is not an instance of the expected domain type.
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
    setObject(index, converter.toStorage(value), jdbcType.jdbcType)
}
