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

    init {
        require(IDENTIFIER_PATTERN.matches(name)) {
            "Invalid table name '$name'. Only letters, digits, and underscores are allowed, and the first character cannot be a digit."
        }
        require(columns.isNotEmpty()) {
            "Table $name must define at least one column."
        }
        require(columns.map { it.name }.distinct().size == columns.size) {
            "Table $name cannot contain duplicate column names."
        }
    }

    companion object {
        private val IDENTIFIER_PATTERN = Regex("[A-Za-z_][A-Za-z0-9_]*")
    }

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
