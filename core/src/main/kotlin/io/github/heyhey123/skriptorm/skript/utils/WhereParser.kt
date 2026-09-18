package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.config.Node
import ch.njol.skript.config.SectionNode
import ch.njol.skript.lang.Expression
import io.github.heyhey123.skriptorm.condition.Condition
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.skript.utils.ExpressionsHelper.parseExpressionNonNull
import io.github.heyhey123.skriptorm.skript.utils.ExpressionsHelper.parseNullableExpression
import io.github.heyhey123.skriptorm.skript.utils.WhereParser.collectFromSection
import io.github.heyhey123.skriptorm.table.NumericValues
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.nbt.NbtSupport
import org.bukkit.event.Event

/**
 * The parsed condition, which can be resolved to a Condition by evaluating expressions with a given event.
 */
sealed class ParsedCondition {

    abstract val table: Table

    abstract fun resolve(event: Event?): Condition

    protected fun resolveValueExpr(
        columnName: String,
        valueExpr: Expression<*>?,
        event: Event?
    ): Any? {
        val column = ExpressionsHelper.requireColumn(table, columnName)
        // A compound from SkBee belongs to a live object and only means what it meant when the script
        // named it. Normalising it here, while the event is at hand, is what lets the query layer run
        // later and elsewhere.
        val value = NbtSupport.normalize(valueExpr?.getSingle(event))
        // The comparison value is narrowed like a written one, and for the same reason: parsed as a
        // number, it is still the number the script wrote here, so a value the column cannot hold is
        // refused rather than quietly compared as something else.
        return if (value is Number) NumericValues.narrow(column, value) else value
    }

    data class Equals(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>?
    ) : ParsedCondition() {

        override fun resolve(event: Event?) = Condition.Equals(
            columnName,
            resolveValueExpr(columnName, valueExpr, event)
        )
    }

    data class NotEquals(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>?
    ) : ParsedCondition() {

        override fun resolve(event: Event?) = Condition.NotEquals(
            columnName,
            resolveValueExpr(columnName, valueExpr, event)
        )
    }

    data class GreaterThan(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>
    ) : ParsedCondition() {

        override fun resolve(event: Event?) = Condition.GreaterThan(
            columnName,
            resolveValueExpr(columnName, valueExpr, event)!!
        )
    }

    data class LessThan(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>
    ) : ParsedCondition() {

        override fun resolve(event: Event?) = Condition.LessThan(
            columnName,
            resolveValueExpr(columnName, valueExpr, event)!!
        )
    }

    data class GreaterThanOrEquals(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>
    ) : ParsedCondition() {

        override fun resolve(event: Event?) = Condition.GreaterThanOrEquals(
            columnName,
            resolveValueExpr(columnName, valueExpr, event)!!
        )
    }

    data class LessThanOrEquals(
        override val table: Table,
        val columnName: String,
        val valueExpr: Expression<*>
    ) : ParsedCondition() {

        override fun resolve(event: Event?) = Condition.LessThanOrEquals(
            columnName,
            resolveValueExpr(columnName, valueExpr, event)!!
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
            resolveValueExpr(columnName, startExpr, event)!!,
            resolveValueExpr(columnName, endExpr, event)!!
        )
    }
}

/**
 * The parsed where clause, which can be resolved to a WhereClause by evaluating expressions with a given event.
 * The evaluation of [Expression] didn't happen yet, so the expressions are still present in the parsed where clause.
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

/**
 * A parser for creating WHERE clauses([WhereClause]) from raw condition strings.
 */
object WhereParser {

    /**
     * Locates the header of a `where` block, for example `where`, `where any` or `where not all`.
     *
     * The check covers the whole keyword, so a value or condition on a column whose name merely
     * starts with `where`, for example `where_clause: 1`, is not mistaken for a `where` block.
     * It deliberately stays loose about what follows the keyword, so that a mistyped header is still
     * recognised as a `where` block and can be reported by [collectFromSection] instead of being
     * silently ignored.
     */
    private val WHERE_SECTION_PATTERN = Regex("^where(\\s.*)?$", RegexOption.IGNORE_CASE)

    /** Parses the header of a `where` block: `where`, `where any`, `where all`, optionally negated. */
    private val WHERE_HEADER_PATTERN = Regex(
        "^where(?:\\s+(?:(?<neg>no|not)\\s+)?(?<mode>any|all))?$",
        RegexOption.IGNORE_CASE
    )

    /**
     * Checks whether a node is the header of a `where` block.
     *
     * @see collectFromSection
     */
    fun isWhereSection(node: Node): Boolean =
        node.key?.let { WHERE_SECTION_PATTERN.matches(it) } == true

    /**
     * Column identifiers follow the same rule as [Table] and [io.github.heyhey123.skriptorm.table.Column]: a Unicode letter, Unicode
     * letter number or underscore, followed by Unicode letters, marks, digits or underscores.
     */
    private const val COLUMN_NAME = "[\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{M}\\p{Nd}_]*"

    private val EQUALS_PATTERN = Regex("""^($COLUMN_NAME)\s*=\s*(.+)$""")
    private val NOT_EQUALS_PATTERN = Regex("""^($COLUMN_NAME)\s*!=\s*(.+)$""")
    private val GREATER_THAN_OR_EQUALS_PATTERN = Regex("""^($COLUMN_NAME)\s*>=\s*(.+)$""")
    private val LESS_THAN_OR_EQUALS_PATTERN = Regex("""^($COLUMN_NAME)\s*<=\s*(.+)$""")
    private val GREATER_THAN_PATTERN = Regex("""^($COLUMN_NAME)\s*>\s*(.+)$""")
    private val LESS_THAN_PATTERN = Regex("""^($COLUMN_NAME)\s*<\s*(.+)$""")
    private val BETWEEN_PATTERN = Regex("""^($COLUMN_NAME)\s+between\s+(.+)\s+and\s+(.+)$""", RegexOption.IGNORE_CASE)

    /**
     * Collect raw conditions from a SectionNode (init phase)
     *
     * @throws IllegalArgumentException if a child is a nested section instead of a single condition
     */
    fun collect(nodes: SectionNode, any: Boolean, neg: Boolean): RawWhereClause? {
        val conditions = mutableListOf<RawCondition>()
        for (node in nodes) {
            val statement = node.key ?: continue
            require(node !is SectionNode) {
                "A where condition must be a single line, but '$statement' is a section."
            }
            conditions.add(RawCondition(statement))
        }
        return if (conditions.isEmpty()) null else RawWhereClause(conditions, any, neg)
    }

    /**
     * Collect raw conditions from a nested `where` section, for example:
     *
     * ```
     * where any:
     *     name = "Alice"
     *     age > 25
     * ```
     *
     * Accepted headers are `where` and `where all` (every condition must match, the default),
     * `where any` (at least one condition must match), and the same two forms negated with
     * `no` or `not`, for example `where no any` or `where not all`. Matching of the header is
     * anchored, so an unknown header is reported instead of being read as some default.
     *
     * @param section the nested `where` section, whose children are the conditions
     * @return the raw clause, or null when the section declares no conditions
     * @throws IllegalArgumentException if the section header is not one of the accepted forms
     */
    fun collectFromSection(section: SectionNode): RawWhereClause? {
        val header = section.key.orEmpty().trim()
        val match = WHERE_HEADER_PATTERN.matchEntire(header)
            ?: throw IllegalArgumentException(
                "Invalid where section '$header'. Expected 'where', 'where any' or 'where all', " +
                    "optionally negated with 'no' or 'not'."
            )
        val mode = match.groups["mode"]?.value
        val any = mode?.equals("any", ignoreCase = true) == true
        val neg = match.groups["neg"] != null
        return collect(section, any, neg)
    }

    /**
     * Parse a condition statement into a ParsedCondition, binding it to the given table.
     */
    fun parseCondition(table: Table, statement: String): ParsedCondition {
        val trimmed = statement.trim()

        NOT_EQUALS_PATTERN.matchEntire(trimmed)?.let {
            val columnName = it.groupValues[1]
            val valueStr = it.groupValues[2]
            return ParsedCondition.NotEquals(table, columnName, parseNullableExpression(table, columnName, valueStr))
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
                parseNullableExpression(table, columnName, valueStr)
            )
        }

        throw IllegalArgumentException("Invalid condition statement: '$statement'")
    }
}
