package io.github.heyhey123.xiaojieorm.table

import io.github.heyhey123.xiaojieorm.type.DataType

/**
 * Represents a column in a database table.
 *
 * @property name The name of the column.
 * @property type The JDBC type of the column.
 * @property isPrimaryKey Indicates if the column is a primary key.
 * @property isAutoIncrement Indicates if the column is auto-incremented. Not allow if isPrimaryKey is false.
 * @property isNullable Indicates if the column can contain null values.
 * @property size The size of the column (if applicable).
 */
data class Column<T : Any>(
    val name: String,
    val type: DataType<T>,
    val isPrimaryKey: Boolean = false,
    val isAutoIncrement: Boolean = false,
    val isNullable: Boolean = true,
    val size: Int? = null
)
