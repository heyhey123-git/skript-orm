package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ParseContext
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.UnparsedLiteral
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.NumericValues
import io.github.heyhey123.skriptorm.table.Table

object ExpressionsHelper {

    /**
     * Gives [expression] a type, so that a bare literal can be read while the script runs.
     *
     * Skript leaves a literal such as the `1` in `by id 1` unparsed until something says what type it
     * should take, and reading one that was never converted throws instead of returning the value. A
     * `%object%` pattern is exactly where that happens, because it is the syntax that has to decide.
     * Anything already parsed comes back as it is.
     */
    @Suppress("UNCHECKED_CAST")
    fun withAnyType(expression: Expression<*>): Expression<Any> {
        if (expression !is UnparsedLiteral) return expression as Expression<Any>
        return (expression.getConvertedExpression(Any::class.java) ?: expression) as Expression<Any>
    }

    /**
     * Resolves [columnName] in [table].
     *
     * @throws IllegalArgumentException if [table] does not declare [columnName]
     */
    fun requireColumn(table: Table, columnName: String): Column<*> =
        table.getColumnByName(columnName)
            ?: throw IllegalArgumentException("Column '$columnName' does not exist in table '${table.name}'")

    /**
     * Parses [valueStr] for the column [columnName] names.
     *
     * A number column is parsed as a number rather than as the exact type the column stores. Skript's
     * conversion into that type narrows: an `int` column turns `5000000000` into `705032704` and a
     * `tinyint` column turns `300` into `44`, both without a word, and a value that has been narrowed
     * cannot be told apart from one that was written that way. [NumericValues.narrow] does the narrowing
     * later instead, where the column is known and a value that does not fit can be refused.
     *
     * @return the parsed expression, or `null` when Skript cannot parse it
     * @throws IllegalArgumentException if [columnName] is not in [table]
     */
    fun parseExpression(table: Table, columnName: String, valueStr: String): Expression<*>? {
        val column = requireColumn(table, columnName)
        val type = if (NumericValues.isNumeric(column.type.domainType)) {
            Number::class.java
        } else {
            column.type.domainType
        }
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
