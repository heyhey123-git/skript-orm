package io.github.heyhey123.skriptorm.table

/**
 * Defines a database table and snapshots its columns into registration order.
 *
 * @property name Unicode identifier used by database implementations.
 * @property columns Columns keyed by name; later changes to the constructor list are not observed.
 * @throws IllegalArgumentException if the name is invalid, no columns are supplied, names are
 * duplicated, or more than one primary key is defined.
 */
class Table(
    val name: String,
    columns: List<Column<*>>
) {

    init {
        require(IDENTIFIER_PATTERN.matches(name)) {
            "Invalid table name '$name'. Identifiers must start with a Unicode letter, Unicode letter number, or underscore, and may then contain Unicode letters, marks, digits, or underscores."
        }
        require(columns.isNotEmpty()) {
            "Table $name must define at least one column."
        }
        require(columns.map { it.name }.distinct().size == columns.size) {
            "Table $name cannot contain duplicate column names."
        }
    }

    companion object {

        private val IDENTIFIER_PATTERN = Regex("[\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{M}\\p{Nd}_]*")
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
     * Gets the column with the specified name.
     *
     * @param columnName The name of the column to retrieve.
     * @return The column if found, null otherwise.
     */
    fun getColumnByName(columnName: String): Column<*>? = columns[columnName]
}
