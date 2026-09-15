package io.github.heyhey123.xiaojieorm.skript.utils

import ch.njol.skript.config.SectionNode
import ch.njol.skript.lang.Expression
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
            val expression = if (rawValue.rawExpression.equals("null", ignoreCase = true)) {
                null
            } else {
                ExpressionsHelper.parseExpressionNonNull(
                    table,
                    rawValue.columnName,
                    rawValue.rawExpression
                )
            }
            parsedMap[rawValue.columnName] = expression
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
    fun bind(table: Table): ParsedValuesList {
        return ParsedValuesList(valuesList.map { it.bind(table) })
    }
}

data class ParsedValues(
    val table: Table,
    val values: Map<String, Expression<*>?>
) {
    fun resolve(event: Event?): Map<String, Any?> {
        val resolvedMap = mutableMapOf<String, Any?>()
        for ((columnName, expression) in values) {
            checkNotNull(table.getColumnByName(columnName)) {
                "Column '$columnName' does not exist in table '${table.name}'"
            }
            resolvedMap[columnName] = expression?.getSingle(event)
        }
        return resolvedMap
    }
}

data class ParsedValuesList(
    val valuesList: List<ParsedValues>
) {
    fun resolve(event: Event?): List<Map<String, Any?>> {
        return valuesList.map { it.resolve(event) }
    }
}

object ValuesParser {
    private val VALUE_PATTERN = Regex("""^\s*(\w+)\s*:\s*(.+)\s*$""")

    /**
     * Collect single row of values (for INSERT ONE / UPDATE).
     */
    fun collectSingle(nodes: SectionNode): RawValues {
        val rawValuesList = mutableListOf<RawValue>()
        for (node in nodes) {
            if (node is SectionNode) continue
            val key = node.key ?: continue
            val match = VALUE_PATTERN.matchEntire(key)
                ?: throw IllegalArgumentException("Invalid value format: '$key'. Expected format: 'columnName: expression'")
            rawValuesList.add(RawValue(match.groupValues[1].trim(), match.groupValues[2].trim()))
        }
        return RawValues(rawValuesList)
    }

    /**
     * Collect multiple rows of values (for INSERT MANY).
     * Expected format:
     * values:
     *     1:
     *         x: 1
     *         y: 2
     *     2:
     *         x: 3
     *         y: 4
     */
    fun collectMultiple(nodes: SectionNode): RawValuesList {
        val allValues = mutableListOf<RawValues>()
        for (node in nodes) {
            if (node is SectionNode) {
                allValues.add(collectSingle(node))
            }
        }
        if (allValues.isEmpty()) {
            throw IllegalArgumentException("No values found in insert many section.")
        }
        return RawValuesList(allValues)
    }

    /**
     * Auto-detect and collect values.
     * Returns a pair: (single values or null, multiple values or null)
     */
    fun collect(nodes: SectionNode): Pair<RawValues?, RawValuesList?> {
        val firstChild = nodes.firstOrNull() ?: return null to null

        // 检查第一个子节点是否是 SectionNode（意味着是 insert many 格式）
        return if (firstChild is SectionNode) {
            null to collectMultiple(nodes)
        } else {
            collectSingle(nodes) to null
        }
    }
}

