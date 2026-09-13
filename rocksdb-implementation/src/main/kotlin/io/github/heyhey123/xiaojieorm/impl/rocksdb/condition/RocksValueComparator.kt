package io.github.heyhey123.xiaojieorm.impl.rocksdb.condition

import io.github.heyhey123.xiaojieorm.impl.rocksdb.type.RocksDataType
import io.github.heyhey123.xiaojieorm.table.Column

/** Compares condition operands using their column's domain type. */
internal object RocksValueComparator {

    fun equals(column: Column<*>, left: Any?, right: Any?): Boolean {
        if (left == null || right == null) return left == right
        if (left is ByteArray && right is ByteArray) return left.contentEquals(right)
        return toDomainValue(column, left) == toDomainValue(column, right)
    }

    @Suppress("UNCHECKED_CAST")
    fun compare(column: Column<*>, left: Any?, right: Any?): Int {
        if (left == null && right == null) return 0
        if (left == null) return -1
        if (right == null) return 1

        val leftValue = toDomainValue(column, left)
        val rightValue = toDomainValue(column, right)
        require(leftValue::class == rightValue::class) {
            "Cannot compare `${leftValue::class.qualifiedName}` with `${rightValue::class.qualifiedName}` " +
                    "for column `${column.name}`."
        }
        val comparable = leftValue as? Comparable<Any>
            ?: throw IllegalArgumentException("Column `${column.name}` does not support ordered comparison.")
        return comparable.compareTo(rightValue)
    }

    @Suppress("UNCHECKED_CAST")
    private fun toDomainValue(column: Column<*>, value: Any): Any {
        if (value !is ByteArray) return value
        val dataType = column.type as? RocksDataType<Any>
            ?: throw IllegalArgumentException("Column `${column.name}` is not backed by a Rocks data type.")
        return dataType.converter.fromStorage(value)
    }
}
