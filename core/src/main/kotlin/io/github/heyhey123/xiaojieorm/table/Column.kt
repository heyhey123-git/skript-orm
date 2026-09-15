package io.github.heyhey123.xiaojieorm.table

import io.github.heyhey123.xiaojieorm.type.DataType

/**
 * Defines a logical column in a database table.
 *
 * @property name Identifier validated with the same Unicode identifier rules as [Table].
 * @property type Logical data type and value converter for the column.
 * @property isPrimaryKey Whether this column is the table primary key.
 * @property isAutoIncrement Whether values are generated automatically; valid only for a primary key.
 * @property isNullable Whether stored values may be null.
 * @property size Optional positive storage size hint.
 * @throws IllegalArgumentException if the identifier, auto-increment combination, or size is invalid.
 */
data class Column<T : Any>(
    val name: String,
    val type: DataType<T>,
    val isPrimaryKey: Boolean = false,
    val isAutoIncrement: Boolean = false,
    val isNullable: Boolean = true,
    val size: Int? = null
) {
    companion object {
        private val IDENTIFIER_PATTERN = Regex("[\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{M}\\p{Nd}_]*")
    }

    init {
        require(IDENTIFIER_PATTERN.matches(name)) {
            "Invalid column name '$name'. Identifiers must start with a Unicode letter, Unicode letter number, or underscore, and may then contain Unicode letters, marks, digits, or underscores."
        }
        require(!isAutoIncrement || isPrimaryKey) {
            "Auto-increment column '$name' must be a primary key."
        }
        require(size == null || size > 0) {
            "Column '$name' size must be greater than zero."
        }
    }
}
