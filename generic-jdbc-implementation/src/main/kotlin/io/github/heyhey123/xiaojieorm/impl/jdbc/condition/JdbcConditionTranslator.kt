package io.github.heyhey123.xiaojieorm.impl.jdbc.condition

import io.github.heyhey123.xiaojieorm.condition.Condition
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.queries.bindValue
import io.github.heyhey123.xiaojieorm.type.DataType
import java.sql.PreparedStatement

/**
 * Translates Condition and WhereClause objects into SQL WHERE clause strings.
 *
 */
object JdbcConditionTranslator {

    /**
     * Translates a WhereClause into its SQL string representation.
     *
     * @param whereClause The WhereClause to translate.
     * @return The SQL string representation of the WHERE clause.
     */
    fun translate(whereClause: WhereClause): String = buildString {
        append("WHERE")
        val conjunction = when (whereClause) {
            is WhereClause.All -> "AND"
            is WhereClause.Any -> "OR"
        }

        if (whereClause.negated) {
            append(" NOT(")
        }

        val conditions = whereClause.conditions
        conditions.forEachIndexed { index, entry ->
            append(" ${translateCondition(entry)}")
            if (index < conditions.size - 1) {
                append(" $conjunction")
            }
        }

        if (whereClause.negated) {
            append(" )")
        }
    }

    /**
     * Translates a Condition into its SQL string representation.
     *
     * @param condition The Condition to translate.
     * @return The SQL string representation of the condition.
     */
    fun translateCondition(condition: Condition): String =
        when (condition) {
            is Condition.Equals -> if (condition.right == null) "${condition.left} IS NULL" else "${condition.left} = ?"
            is Condition.NotEquals -> if (condition.right == null) "${condition.left} IS NOT NULL" else "${condition.left} != ?"
            is Condition.Between -> "${condition.left} BETWEEN ? AND ?"
            is Condition.GreaterThan -> "${condition.left} > ?"
            is Condition.GreaterThanOrEquals -> "${condition.left} >= ?"
            is Condition.LessThan -> "${condition.left} < ?"
            is Condition.LessThanOrEquals -> "${condition.left} <= ?"
        }

    /**
     * Fills the parameters with a PreparedStatement based on the given Condition.
     *
     * @param condition The Condition to fill parameters for.
     * @param statement The PreparedStatement to fill parameters into.
     * @param offset The starting index for parameter filling.
     * @param type The SqlType of the column being compared.
     * @return The next offset after filling the parameters.
     */
    fun fillConditionParameters(condition: Condition, statement: PreparedStatement, offset: Int, type: DataType<*>): Int {
        return when (condition) {
            is Condition.Equals -> {
                if (condition.right == null) offset else {
                    statement.bindValue(offset, condition.right, type)
                    offset + 1
                }
            }

            is Condition.NotEquals -> {
                if (condition.right == null) offset else {
                    statement.bindValue(offset, condition.right, type)
                    offset + 1
                }
            }

            is Condition.Between -> {
                statement.bindValue(offset, condition.start, type)
                statement.bindValue(offset + 1, condition.end, type)
                offset + 2
            }

            is Condition.GreaterThan -> {
                statement.bindValue(offset, condition.right, type)
                offset + 1
            }

            is Condition.GreaterThanOrEquals -> {
                statement.bindValue(offset, condition.right, type)
                offset + 1
            }

            is Condition.LessThan -> {
                statement.bindValue(offset, condition.right, type)
                offset + 1
            }

            is Condition.LessThanOrEquals -> {
                statement.bindValue(offset, condition.right, type)
                offset + 1
            }
        }
    }

}
