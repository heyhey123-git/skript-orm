package io.github.heyhey123.xiaojieorm.impl.jdbc.type

import io.github.heyhey123.xiaojieorm.type.DataType
import java.sql.JDBCType

/**
 * Interface for JDBC data types.
 *
 */
interface JdbcDataType<T : Any> : DataType<T> {

    /**
     * The corresponding JDBC type.
     */
    val jdbcType: JDBCType

    /**
     * The default size for this JDBC type.
     */
    val defaultSize: Int
        get() = -1

    /**
     * Whether a column length can be appended to this storage type.
     */
    val supportsSize: Boolean
        get() = false

    /**
     * The name of the storage type in the database.
     */
    val storageName: String
}
