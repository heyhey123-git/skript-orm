package io.github.heyhey123.xiaojieorm.utils

import io.github.heyhey123.xiaojieorm.condition.Condition
import io.github.heyhey123.xiaojieorm.table.Table

/**
 * A utility object for validating arguments.
 * If an argument is invalid, an [IllegalArgumentException] is thrown.
 */
object ArgumentValidator {
    /**
     * Verifies that all columns referenced in the list of conditions exist in the given table.
     *
     * @param table The table to check against.
     * @param conditions The list of conditions to verify.
     * @throws IllegalArgumentException if any column in the conditions does not exist in the table.
     */
    fun verifyConditionList(table: Table, conditions: List<Condition>) {
        require(conditions.isNotEmpty()) { "Condition list cannot be empty." }

        conditions.forEach {
            val left = it.left
            val column = table.getColumnByName(left)
            require(column != null) { "Column '$left' does not exist in table '${table.name}'." }
        }
    }

    /**
     * Verifies that the provided ID column name matches the primary key column of the given table.
     *
     * @param table The table to check against.
     * @param idColumnName The name of the ID column to verify.
     * @throws IllegalArgumentException if the table does not have a primary key
     * or if the provided ID column name does not match the primary key column name.
     */
    fun verifyIdColumn(table: Table, idColumnName: String) {
        val primaryKeyColumn = table.primaryKey
        require(primaryKeyColumn != null) { "Table '${table.name}' does not have a primary key column." }
        require(primaryKeyColumn.name == idColumnName) {
            "Provided ID column '$idColumnName' does not match the primary key column '${primaryKeyColumn.name}' of table '${table.name}'."
        }
    }
}
