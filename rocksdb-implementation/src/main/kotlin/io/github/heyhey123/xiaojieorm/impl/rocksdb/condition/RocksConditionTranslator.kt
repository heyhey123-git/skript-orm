package io.github.heyhey123.xiaojieorm.impl.rocksdb.condition

import io.github.heyhey123.xiaojieorm.condition.Condition
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.table.Table

/**
 * 条件翻译结果，区分主键快速路径和全表扫描
 */
sealed class TranslateResult {

    /**
     * 主键等值查询，可直接定位
     */
    data class PrimaryKeyLookup(val pkValue: Any) : TranslateResult()

    /**
     * 需要扫描过滤
     */
    data class ScanWithFilter(val predicate: (Map<String, Any?>) -> Boolean) : TranslateResult()
}

object RocksConditionTranslator {

    /**
     * 翻译 where 子句，尝试提取主键优化
     */
    fun translate(where: WhereClause?, table: Table): TranslateResult {
        if (where == null || where.conditions.isEmpty()) {
            return TranslateResult.ScanWithFilter { true }
        }

        // 尝试主键优化：仅当 All 条件、未取反、只有一个主键等值条件时
        val pkColumn = table.primaryKey
        if (pkColumn != null && where is WhereClause.All && !where.negated) {
            val pkCondition = where.conditions.singleOrNull {
                it is Condition.Equals && it.left == pkColumn.name
            } as? Condition.Equals

            if (pkCondition != null && pkCondition.right != null) {
                return TranslateResult.PrimaryKeyLookup(pkCondition.right!!)
            }
        }

        // 回退到扫描过滤
        return TranslateResult.ScanWithFilter(buildPredicate(where))
    }

    /**
     * Builds a predicate function from the given WhereClause.
     *
     * @param where The WhereClause to translate.
     * @return A predicate function that takes a row (as a map of column names to values)
     */
    private fun buildPredicate(where: WhereClause): (Map<String, Any?>) -> Boolean {
        val predicates = where.conditions.map(::translateCondition)

        val combined: (Map<String, Any?>) -> Boolean = when (where) {
            is WhereClause.All -> { row -> predicates.all { it(row) } }
            is WhereClause.Any -> { row -> predicates.any { it(row) } }
        }

        return if (where.negated) {
            { row -> !combined(row) }
        } else {
            combined
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun translateCondition(condition: Condition): (Map<String, Any?>) -> Boolean =
        when (condition) {
            is Condition.Equals -> { row -> row[condition.left] == condition.right }
            is Condition.NotEquals -> { row -> row[condition.left] != condition.right }
            is Condition.GreaterThan -> { row ->
                compareValues(row[condition.left], condition.right) > 0
            }
            is Condition.GreaterThanOrEquals -> { row ->
                compareValues(row[condition.left], condition.right) >= 0
            }
            is Condition.LessThan -> { row ->
                compareValues(row[condition.left], condition.right) < 0
            }
            is Condition.LessThanOrEquals -> { row ->
                compareValues(row[condition.left], condition.right) <= 0
            }
            is Condition.Between -> { row ->
                val value = row[condition.left]
                compareValues(value, condition.start) >= 0 &&
                        compareValues(value, condition.end) <= 0
            }
        }

    @Suppress("UNCHECKED_CAST")
    private fun compareValues(left: Any?, right: Any?): Int {
        if (left == null && right == null) return 0
        if (left == null) return -1
        if (right == null) return 1
        return (left as Comparable<Any>).compareTo(right)
    }
}
