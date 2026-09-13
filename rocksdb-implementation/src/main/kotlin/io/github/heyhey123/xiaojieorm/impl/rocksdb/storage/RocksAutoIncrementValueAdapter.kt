package io.github.heyhey123.xiaojieorm.impl.rocksdb.storage

import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.type.TypeId

/**
 * Adapts between the internal `Long` sequence representation and supported
 * auto-increment column-domain values.
 */
object RocksAutoIncrementValueAdapter {

    /**
     * Converts a sequence value to the declared column-domain type.
     *
     * @throws IllegalArgumentException if the column type is unsupported or the
     * sequence value is outside the column's numeric range.
     */
    fun toColumnValue(column: Column<*>, sequenceValue: Long): Any =
        when (column.type.typeCode) {
            TypeId.TINYINT.code -> toByte(column, sequenceValue)
            TypeId.INT.code -> toInt(column, sequenceValue)
            TypeId.BIGINT.code -> sequenceValue
            else -> throw unsupportedColumnType(column)
        }

    /**
     * Converts an explicit column-domain value to the internal sequence type.
     *
     * This conversion is intentionally strict: the value must already match the
     * domain type declared by the column.
     */
    fun toSequenceValue(column: Column<*>, columnValue: Any): Long =
        when (column.type.typeCode) {
            TypeId.TINYINT.code -> requireType<Byte>(column, columnValue).toLong()
            TypeId.INT.code -> requireType<Int>(column, columnValue).toLong()
            TypeId.BIGINT.code -> requireType<Long>(column, columnValue)
            else -> throw unsupportedColumnType(column)
        }

    private fun toByte(column: Column<*>, value: Long): Byte {
        require(value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
            "Auto-increment value $value exceeds TINYINT range for column `${column.name}`."
        }
        return value.toByte()
    }

    private fun toInt(column: Column<*>, value: Long): Int {
        require(value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            "Auto-increment value $value exceeds INT range for column `${column.name}`."
        }
        return value.toInt()
    }

    private inline fun <reified T : Any> requireType(column: Column<*>, value: Any): T =
        value as? T ?: throw IllegalArgumentException(
            "Auto-increment value for column `${column.name}` has invalid domain type " +
                "`${value::class.qualifiedName}`; expected `${T::class.qualifiedName}`."
        )

    private fun unsupportedColumnType(column: Column<*>) = IllegalArgumentException(
        "Auto-increment column `${column.name}` must use tinyint, int, or bigint, " +
            "but was `${column.type.typeCode}`."
    )
}
