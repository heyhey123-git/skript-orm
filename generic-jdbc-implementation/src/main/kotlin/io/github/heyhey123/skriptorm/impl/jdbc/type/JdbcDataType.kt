package io.github.heyhey123.skriptorm.impl.jdbc.type

import io.github.heyhey123.skriptorm.type.DataType
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
     * Whether a column the server reports as [storedSize] units can serve this storage declared with
     * [declaredSize] units.
     *
     * A declaration asks for a storage that can hold what the script writes into it, so a column with more
     * room than the declaration asks for serves it — but only where the extra room is room. A fixed-length
     * `BINARY(n)` column reads back padded to its full width, so the extra room of a wider one arrives at the
     * converter as trailing bytes rather than as space to spare, which is why the default answer here is the
     * exact one and only a variable-length storage overrides it.
     */
    fun servesSize(storedSize: Int, declaredSize: Int): Boolean = storedSize == declaredSize

    /**
     * The name of the storage type in the database.
     */
    val storageName: String
}
