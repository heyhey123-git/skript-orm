package io.github.heyhey123.xiaojieorm.skript.utils

import ch.njol.skript.config.SectionNode
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ParseContext
import ch.njol.skript.lang.SkriptParser
import io.github.heyhey123.xiaojieorm.condition.Condition
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.skript.utils.ExpressionsHelper.parseExpression
import io.github.heyhey123.xiaojieorm.skript.utils.ExpressionsHelper.parseExpressionNonNull
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

/**
 * 预解析的条件，保存表达式而非值
 */
sealed class ParsedCondition {
    abstract val table: Table

    abstract fun resolve(event: Event?): Condition

    protected fun resolveValueExpr(
        columnName: String,
        valueExpr: Expression<*>?,
        event: Event?
    ): Any? {
        checkNotNull(table.getColumnByName(columnName)) {
            "Column '$columnName' does not exist in table '${table.name}'"
        }
        return valueExpr?.getSingle(event)
    }

    data class Equals(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>?
    ) : ParsedCondition() {
        override fun resolve(event: Event?) = Condition.Equals(
            columnName,
            resolveValueExpr(columnName,valueExpr, event)
        )
    }

    data class NotEquals(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>?
    ) : ParsedCondition() {
        override fun resolve(event: Event?) = Condition.NotEquals(
            columnName,
            resolveValueExpr(columnName,valueExpr, event)
        )
    }

    data class GreaterThan(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>
    ) : ParsedCondition() {
        override fun resolve(event: Event?) = Condition.GreaterThan(
            columnName,
            resolveValueExpr(columnName,valueExpr, event)!!
        )
    }

    data class LessThan(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>
    ) : ParsedCondition() {
        override fun resolve(event: Event?) = Condition.LessThan(
            columnName,
            resolveValueExpr(columnName,valueExpr, event)!!
        )
    }

    data class GreaterThanOrEquals(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>
    ) : ParsedCondition() {
        override fun resolve(event: Event?) = Condition.GreaterThanOrEquals(
            columnName,
            resolveValueExpr(columnName,valueExpr, event)!!
        )
    }

    data class LessThanOrEquals(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>
    ) : ParsedCondition() {
        override fun resolve(event: Event?) = Condition.LessThanOrEquals(
            columnName,
            resolveValueExpr(columnName,valueExpr, event)!!
        )
    }

    data class Between(
        override val table: Table,
        val columnName: String,
        val startExpr: Expression<*>,
        val endExpr: Expression<*>
    ) : ParsedCondition() {
        override fun resolve(event: Event?) = Condition.Between(
            columnName,
            resolveValueExpr(columnName,startExpr, event)!!,
            resolveValueExpr(columnName,endExpr, event)!!
        )
    }
}

/**
 * 预解析的 WHERE 子句
 */
sealed class ParsedWhereClause {

    /**
     * Try to resolve to a WhereClause by evaluating expressions with the given event.
     *
     * @param event the event to evaluate expressions against
     * @return the resolved WhereClause
     * @throws UnsupportedOperationException if the clause cannot be resolved
     */
    abstract fun resolve(event: Event?): WhereClause

    data class Any(val neg: Boolean, val conditions: List<ParsedCondition>) : ParsedWhereClause() {
        override fun resolve(event: Event?) = WhereClause.Any(neg, conditions.map { it.resolve(event) })
    }

    data class All(val neg: Boolean, val conditions: List<ParsedCondition>) : ParsedWhereClause() {
        override fun resolve(event: Event?) = WhereClause.All(neg, conditions.map { it.resolve(event) })
    }
}

/**
 * The raw condition statement before binding to a table
 */
data class RawCondition(val statement: String)

/**
 * The raw where clause before binding to a table
 */
data class RawWhereClause(
    val conditions: List<RawCondition>,
    val any: Boolean,
    val neg: Boolean
) {
    /**
     * Bind the raw where clause to a specific table, parsing the condition statements.
     */
    fun bind(table: Table): ParsedWhereClause {
        val parsed = conditions.map { WhereParser.parseCondition(table, it.statement) }
        return if (any) ParsedWhereClause.Any(neg, parsed) else ParsedWhereClause.All(neg, parsed)
    }
}

object WhereParser {
    private val EQUALS_PATTERN = Regex("""^(\w+)\s*=\s*(.+)$""")
    private val NOT_EQUALS_PATTERN = Regex("""^(\w+)\s*!=\s*(.+)$""")
    private val GREATER_THAN_OR_EQUALS_PATTERN = Regex("""^(\w+)\s*>=\s*(.+)$""")
    private val LESS_THAN_OR_EQUALS_PATTERN = Regex("""^(\w+)\s*<=\s*(.+)$""")
    private val GREATER_THAN_PATTERN = Regex("""^(\w+)\s*>\s*(.+)$""")
    private val LESS_THAN_PATTERN = Regex("""^(\w+)\s*<\s*(.+)$""")
    private val BETWEEN_PATTERN = Regex("""^(\w+)\s+between\s+(.+)\s+and\s+(.+)$""", RegexOption.IGNORE_CASE)

    /**
     * Collect raw conditions from a SectionNode (init phase)
     */
    fun collect(nodes: SectionNode, any: Boolean, neg: Boolean): RawWhereClause? {
        val conditions = mutableListOf<RawCondition>()
        for (node in nodes) {
            val statement = node.key ?: continue
            conditions.add(RawCondition(statement))
        }
        return if (conditions.isEmpty()) null else RawWhereClause(conditions, any, neg)
    }

    /**
     * Parse a condition statement into a ParsedCondition, binding it to the given table.
     */
    fun parseCondition(table: Table, statement: String): ParsedCondition {
        val trimmed = statement.trim()

        NOT_EQUALS_PATTERN.matchEntire(trimmed)?.let {
            val columnName = it.groupValues[1]
            val valueStr = it.groupValues[2]
            return ParsedCondition.NotEquals(table, columnName, parseExpression(table, columnName, valueStr))
        }

        GREATER_THAN_OR_EQUALS_PATTERN.matchEntire(trimmed)?.let {
            val columnName = it.groupValues[1]
            val valueStr = it.groupValues[2]
            return ParsedCondition.GreaterThanOrEquals(table, columnName, parseExpressionNonNull(table, columnName, valueStr))
        }

        LESS_THAN_OR_EQUALS_PATTERN.matchEntire(trimmed)?.let {
            val columnName = it.groupValues[1]
            val valueStr = it.groupValues[2]
            return ParsedCondition.LessThanOrEquals(table, columnName, parseExpressionNonNull(table, columnName, valueStr))
        }

        GREATER_THAN_PATTERN.matchEntire(trimmed)?.let {
            val columnName = it.groupValues[1]
            val valueStr = it.groupValues[2]
            return ParsedCondition.GreaterThan(table, columnName, parseExpressionNonNull(table, columnName, valueStr))
        }

        LESS_THAN_PATTERN.matchEntire(trimmed)?.let {
            val columnName = it.groupValues[1]
            val valueStr = it.groupValues[2]
            return ParsedCondition.LessThan(
                table,
                columnName,
                parseExpressionNonNull(table, columnName, valueStr)
            )
        }

        BETWEEN_PATTERN.matchEntire(trimmed)?.let {
            val columnName = it.groupValues[1]
            val startValueStr = it.groupValues[2]
            val endValueStr = it.groupValues[3]
            return ParsedCondition.Between(
                table,
                columnName,
                parseExpressionNonNull(table, columnName, startValueStr),
                parseExpressionNonNull(table, columnName, endValueStr)
            )
        }

        EQUALS_PATTERN.matchEntire(trimmed)?.let {
            val columnName = it.groupValues[1]
            val valueStr = it.groupValues[2]
            return ParsedCondition.Equals(
                table,
                columnName,
                parseExpression(table, columnName, valueStr)
            )
        }

        throw IllegalArgumentException("Invalid condition statement: '$statement'")
    }
}
