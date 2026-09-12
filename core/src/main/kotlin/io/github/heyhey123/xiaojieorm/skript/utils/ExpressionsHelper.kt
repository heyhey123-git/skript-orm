package io.github.heyhey123.xiaojieorm.skript.utils

import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ParseContext
import ch.njol.skript.lang.SkriptParser
import io.github.heyhey123.xiaojieorm.table.Table

object ExpressionsHelper {

    /**
     * Parse an expression string for a given column in the table.
     *
     * @param table the table containing the column
     * @param columnName the name of the column
     * @param valueStr the expression string to parse
     * @return the parsed Expression, or null if parsing fails
     */
    fun parseExpression(table: Table, columnName: String, valueStr: String): Expression<*>? {
        val column = table.getColumnByName(columnName)
            ?: throw IllegalArgumentException("Column '$columnName' does not exist in table '${table.name}'")
        val type = column.type.domainType
        return SkriptParser(valueStr, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(type)
    }

    /**
     * Try to parse an expression string for a given column in the table, throwing if it fails.
     *
     * @param table the table containing the column
     * @param columnName the name of the column
     * @param valueStr the expression string to parse
     * @return the parsed Expression
     * @throws IllegalArgumentException if parsing fails
     */
    fun parseExpressionNonNull(table: Table, columnName: String, valueStr: String): Expression<*> {
        return parseExpression(table, columnName, valueStr)
            ?: throw IllegalArgumentException("Cannot parse expression for column '$columnName': $valueStr")
    }
}
