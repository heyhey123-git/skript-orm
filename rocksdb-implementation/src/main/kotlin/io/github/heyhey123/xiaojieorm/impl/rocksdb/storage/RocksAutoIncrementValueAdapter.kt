package io.github.heyhey123.xiaojieorm.impl.rocksdb.storage

import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.type.TypeId

object RocksAutoIncrementValueAdapter {

    fun toColumnValue(column: Column<*>, sequenceValue: Long): Any = when (column.type.typeCode) {
        TypeId.TINYINT.code -> {
            require(sequenceValue in Byte.MIN_VALUE..Byte.MAX_VALUE) {
                "Auto-increment value $sequenceValue exceeds TINYINT range for column `${column.name}`."
            }
            sequenceValue.toByte()
        }

        TypeId.INT.code -> {
            require(sequenceValue in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                "Auto-increment value $sequenceValue exceeds INT range for column `${column.name}`."
            }
            sequenceValue.toInt()
        }

        TypeId.BIGINT.code -> sequenceValue

        else -> throw IllegalArgumentException(
            "Auto-increment column `${column.name}` must use tinyint, int, or bigint, but was `${column.type.typeCode}`."
        )
    }
}
