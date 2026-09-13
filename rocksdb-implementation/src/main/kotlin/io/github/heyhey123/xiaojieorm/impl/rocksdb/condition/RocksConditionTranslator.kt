package io.github.heyhey123.xiaojieorm.impl.rocksdb.condition

import io.github.heyhey123.xiaojieorm.condition.Condition
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table

sealed class TranslateResult {
    data class PrimaryKeyLookup(val pkValue: Any) : TranslateResult()
    data class ScanWithFilter(val predicate: (Map<String, Any?>) -> Boolean) : TranslateResult()
}

object RocksConditionTranslator {

    fun translate(where: WhereClause?, table: Table): TranslateResult {
        if (where == null || where.conditions.isEmpty()) {
            return TranslateResult.ScanWithFilter { true }
        }

        val primaryKey = table.primaryKey
        if (primaryKey != null && where is WhereClause.All && !where.negated) {
            val condition = where.conditions.singleOrNull() as? Condition.Equals
            val right = condition?.right
            if (condition?.left == primaryKey.name && right != null) {
                return TranslateResult.PrimaryKeyLookup(right)
            }
        }

        return TranslateResult.ScanWithFilter(buildPredicate(where, table))
    }

    private fun buildPredicate(
        where: WhereClause,
        table: Table
    ): (Map<String, Any?>) -> Boolean {
        val predicates = where.conditions.map { translateCondition(it, table) }
        val combined: (Map<String, Any?>) -> Boolean = when (where) {
            is WhereClause.All -> { row -> predicates.all { it(row) } }
            is WhereClause.Any -> { row -> predicates.any { it(row) } }
        }
        return if (where.negated) ({ row -> !combined(row) }) else combined
    }

    private fun translateCondition(
        condition: Condition,
        table: Table
    ): (Map<String, Any?>) -> Boolean {
        val column = requireColumn(table, condition.left)
        return when (condition) {
            is Condition.Equals -> { row ->
                RocksValueComparator.equals(column, row[condition.left], condition.right)
            }

            is Condition.NotEquals -> { row ->
                !RocksValueComparator.equals(column, row[condition.left], condition.right)
            }

            is Condition.GreaterThan -> ordered(column, condition.left, condition.right) { it > 0 }
            is Condition.GreaterThanOrEquals -> ordered(column, condition.left, condition.right) { it >= 0 }
            is Condition.LessThan -> ordered(column, condition.left, condition.right) { it < 0 }
            is Condition.LessThanOrEquals -> ordered(column, condition.left, condition.right) { it <= 0 }
            is Condition.Between -> { row ->
                RocksValueComparator.compare(column, row[condition.left], condition.start) >= 0 &&
                        RocksValueComparator.compare(column, row[condition.left], condition.end) <= 0
            }
        }
    }

    private fun ordered(
        column: Column<*>,
        columnName: String,
        right: Any,
        accepts: (Int) -> Boolean
    ): (Map<String, Any?>) -> Boolean = { row ->
        accepts(RocksValueComparator.compare(column, row[columnName], right))
    }

    private fun requireColumn(table: Table, name: String): Column<*> =
        table.columns[name]
            ?: throw IllegalArgumentException("Column `$name` does not exist in table `${table.name}`.")
}
