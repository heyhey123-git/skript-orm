package io.github.heyhey123.xiaojieorm.table

/**
 * Represents a database table.
 *
 * @property name The name of the table.
 * @property columns The columns in the table.
 * @throws IllegalArgumentException if more than one primary key column is defined.
 */
class Table(
    val name: String,
    columns: List<Column<*>>
) {

    /**
     * A map of column names to their corresponding Column objects.
     */
    val columns: Map<String, Column<*>> = columns.associateBy { it.name }

    /**
     * The primary key column of the table, if any.
     */
    val primaryKey = run {
        val primaryKeys = columns.filter { it.isPrimaryKey }
        require(primaryKeys.size <= 1) {
            "Table $name cannot have more than one primary key column."
        }
        primaryKeys.firstOrNull()
    }

    /**
     * Checks if the table has a column with the specified name.
     *
     * @param columnName The name of the column to check.
     * @return True if the column exists, false otherwise.
     */
    fun hasColumn(columnName: String): Boolean = columnName in columns

    /**
     * Gets the column with the specified name.
     *
     * @param columnName The name of the column to retrieve.
     * @return The column if found, null otherwise.
     */
    fun getColumnByName(columnName: String): Column<*>? = columns[columnName]
}
