package io.github.heyhey123.xiaojieorm.skript.utils

import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ParseContext
import ch.njol.skript.lang.SkriptParser
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table

object ExpressionsHelper {

    /**
     * Resolves [columnName] in [table].
     *
     * @throws IllegalArgumentException if [table] does not declare [columnName]
     */
    fun requireColumn(table: Table, columnName: String): Column<*> =
        table.getColumnByName(columnName)
            ?: throw IllegalArgumentException("Column '$columnName' does not exist in table '${table.name}'")

    /**
     * Parses [valueStr] as the domain type of [columnName].
     *
     * @return the parsed expression, or `null` when Skript cannot parse it
     * @throws IllegalArgumentException if [columnName] is not in [table]
     */
    fun parseExpression(table: Table, columnName: String, valueStr: String): Expression<*>? {
        val type = requireColumn(table, columnName).type.domainType
        return SkriptParser(valueStr, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(type)
    }

    /**
     * Try to parse an expression string for a given column in the table, throwing if it fails.
     *
     * @param table the table containing the column
     * @param columnName the name of the column
     * @param valueStr the expression string to parse
     * @return the parsed Expression
     * @throws IllegalArgumentException if [columnName] is not in [table], or parsing fails
     */
    fun parseExpressionNonNull(table: Table, columnName: String, valueStr: String): Expression<*> = parseExpression(table, columnName, valueStr)
        ?: throw IllegalArgumentException("Cannot parse expression for column '$columnName': $valueStr")

    /**
     * Parses [valueStr] for [columnName], where a literal `null` means SQL NULL and therefore has no
     * expression at all.
     *
     * The column is resolved in both cases. A literal `null` skips expression parsing, so without
     * this check a misspelled column would be reported only when the statement runs, while the same
     * typo next to a spelled-out value is reported while the script is parsed. The two forms have to
     * fail at the same point, or the error surfaces far away from the line that caused it.
     *
     * @return the parsed expression, or `null` for a literal `null`
     * @throws IllegalArgumentException if [columnName] is not in [table]
     */
    fun parseNullableExpression(table: Table, columnName: String, valueStr: String): Expression<*>? =
        if (valueStr.trim().equals("null", ignoreCase = true)) {
            requireColumn(table, columnName)
            null
        } else {
            parseExpressionNonNull(table, columnName, valueStr)
        }
}
