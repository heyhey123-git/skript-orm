package io.github.heyhey123.xiaojieorm.skript.utils

import ch.njol.skript.config.Node
import ch.njol.skript.config.SectionNode
import ch.njol.skript.lang.Expression
import io.github.heyhey123.xiaojieorm.skript.utils.ValuesParser.requireValuesHeader
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

data class RawValue(
    val columnName: String,
    val rawExpression: String
)

data class RawValues(
    val values: List<RawValue>
) {

    fun bind(table: Table): ParsedValues {
        val parsedMap = mutableMapOf<String, Expression<*>?>()
        for (rawValue in values) {
            // A literal `null` is the only way to store SQL NULL. It becomes a present key whose
            // value is null, which the query layer binds as NULL. A list variable cannot express
            // this, because Skript removes a key that is set to null, and the write sections read an
            // absent key as "not supplied" rather than as NULL.
            parsedMap[rawValue.columnName] = ExpressionsHelper.parseNullableExpression(
                table,
                rawValue.columnName,
                rawValue.rawExpression
            )
        }
        return ParsedValues(table, parsedMap)
    }
}

/**
 * Raw values list for insert many operations.
 */
data class RawValuesList(
    val valuesList: List<RawValues>
) {

    fun bind(table: Table): ParsedValuesList = ParsedValuesList(valuesList.map { it.bind(table) })
}

data class ParsedValues(
    val table: Table,
    val values: Map<String, Expression<*>?>
) {

    fun resolve(event: Event?): Map<String, Any?> {
        val resolvedMap = mutableMapOf<String, Any?>()
        for ((columnName, expression) in values) {
            ExpressionsHelper.requireColumn(table, columnName)
            resolvedMap[columnName] = expression?.getSingle(event)
        }
        return resolvedMap
    }
}

data class ParsedValuesList(
    val valuesList: List<ParsedValues>
) {

    fun resolve(event: Event?): List<Map<String, Any?>> = valuesList.map { it.resolve(event) }
}

object ValuesParser {

    /**
     * Column identifiers follow the same rule as [Table] and [io.github.heyhey123.xiaojieorm.table.Column]: a Unicode letter, Unicode
     * letter number or underscore, followed by Unicode letters, marks, digits or underscores.
     */
    private const val COLUMN_NAME = "[\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{M}\\p{Nd}_]*"

    private val VALUE_PATTERN = Regex("""^\s*($COLUMN_NAME)\s*:\s*(.+)\s*$""")

    /**
     * Locates the header of a `values` block.
     *
     * The check covers the whole keyword, so a value on a column whose name merely starts with
     * `values`, for example `values_count: 1`, is not mistaken for a `values` block. It deliberately
     * stays loose about what follows the keyword, so that a mistyped header is still recognised as a
     * `values` block and can be reported by [requireValuesHeader] instead of being read as a value.
     */
    private val VALUES_SECTION_PATTERN = Regex("^values(\\s.*)?$", RegexOption.IGNORE_CASE)

    /** Parses the header of a `values` block, which accepts the bare keyword only. */
    private val VALUES_HEADER_PATTERN = Regex("^values$", RegexOption.IGNORE_CASE)

    /**
     * Checks whether a node is the header of a `values` block.
     *
     * @see requireValuesHeader
     */
    fun isValuesSection(node: Node): Boolean =
        node.key?.let { VALUES_SECTION_PATTERN.matches(it) } == true

    /**
     * Validates the header of an explicit `values` block.
     *
     * @param section the `values` block whose header is validated
     * @throws IllegalArgumentException if the header is not the bare `values` keyword
     */
    fun requireValuesHeader(section: SectionNode) {
        val header = section.key.orEmpty().trim()
        require(VALUES_HEADER_PATTERN.matches(header)) {
            "Invalid values section '$header'. Expected 'values'."
        }
    }

    /**
     * Collects the values of a write section.
     *
     * @param nodes the entries holding the values: the children of an explicit `values` block, or the
     *        body of the write section when no such block is used
     * @param supportsMultipleRows whether the operation accepts one row per nested block
     * @return the single row, the rows, or null when [nodes] declares no values at all
     * @throws IllegalArgumentException if a value is malformed, if a nested block is used where a
     *         single value is expected, or if rows and single values are mixed
     */
    fun collect(nodes: List<Node>, supportsMultipleRows: Boolean): Pair<RawValues?, RawValuesList?>? {
        if (nodes.isEmpty()) return null

        val rows = nodes.filterIsInstance<SectionNode>()
        if (rows.isEmpty()) return collectSingle(nodes) to null

        require(supportsMultipleRows) {
            "This write operation expects single values, but '${rows.first().key}' is a block."
        }
        val singleValue = nodes.firstOrNull { it !is SectionNode }
        require(singleValue == null) {
            "Rows and single values cannot be mixed: '${singleValue?.key}' is not a row block."
        }
        return null to collectMultiple(rows)
    }

    /**
     * Collects a single row of values (for INSERT ONE / UPDATE).
     *
     * @param nodes the entries of the row, each written as `column: expression`
     * @throws IllegalArgumentException if an entry is malformed or is a nested block
     */
    fun collectSingle(nodes: List<Node>): RawValues {
        val rawValues = nodes.map { node ->
            require(node !is SectionNode) {
                "A value must be a single line, but '${node.key}' is a block."
            }
            val key = requireNotNull(node.key) { "A value entry cannot be empty." }
            val match = VALUE_PATTERN.matchEntire(key)
                ?: throw IllegalArgumentException(
                    "Invalid value '$key'. Expected 'column: expression'."
                )
            RawValue(match.groupValues[1].trim(), match.groupValues[2].trim())
        }
        return RawValues(rawValues)
    }

    /**
     * Collects one row per nested block (for INSERT MANY).
     *
     * @param rows the row blocks, for example:
     * ```
     * values:
     *     1:
     *         name: "Alice"
     *     2:
     *         name: "Bob"
     * ```
     * @throws IllegalArgumentException if a row declares no values
     */
    fun collectMultiple(rows: List<SectionNode>): RawValuesList {
        val valuesList = rows.map { row ->
            val values = collectSingle(row.toList())
            require(values.values.isNotEmpty()) {
                "Row '${row.key}' must declare at least one value."
            }
            values
        }
        return RawValuesList(valuesList)
    }
}
