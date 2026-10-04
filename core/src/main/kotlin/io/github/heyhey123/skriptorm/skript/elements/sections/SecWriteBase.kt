package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.skript.lang.Variable
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.SkriptOrm
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.skript.utils.AffectedRows
import io.github.heyhey123.skriptorm.skript.utils.ConnectionScope
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.RawValues
import io.github.heyhey123.skriptorm.skript.utils.RawValuesList
import io.github.heyhey123.skriptorm.skript.utils.RawWhereClause
import io.github.heyhey123.skriptorm.skript.utils.ValuesParser
import io.github.heyhey123.skriptorm.skript.utils.VariableValuesReader
import io.github.heyhey123.skriptorm.skript.utils.WhereParser
import io.github.heyhey123.skriptorm.table.Table
import org.bukkit.event.Event

/**
 * Common parsing and execution for write sections.
 *
 * A write either reads its values from the section body, from a list variable given as an expression,
 * or has no values at all, as a delete does. The first two sources are mutually exclusive, and both
 * are resolved on the main thread into the same rows before the operation is dispatched.
 *
 * ## Missing columns
 *
 * Both sources produce maps keyed by column name, but they handle null differently:
 *
 * - In a section body, an explicit `null` remains in the map and is bound as SQL NULL.
 * - In a list variable, Skript removes keys set to null. An absent key cannot be distinguished
 *   from one explicitly set to null.
 *
 * An absent key is omitted from the statement. Updates leave that column unchanged; inserts let
 * the database apply its default. `insert many` is the exception: all rows in one statement need
 * the same column set. See [VariableValuesReader] for that behavior.
 *
 * Treating missing keys as null would silently clear columns omitted from a dynamically built
 * variable. This API does not offer full-row replacement.
 *
 * ## The affected row count
 *
 * [AffectedRows.PATTERN] lets a script store the number of affected rows in a variable. The variable
 * is cleared before the write and set after a successful statement with an exact count. See
 * [AffectedRows] for details.
 */
abstract class SecWriteBase : Section() {

    protected lateinit var tableNameExpr: Expression<String>

    /**
     * The variable the affected row count is stored in, or null when the statement did not ask for one.
     */
    protected var affectedRowsVariable: Variable<*>? = null

    /** Values for single-row writes. Resolves to the columns the author wrote, and only those. */
    protected var singleValues: RawValues? = null

    /** Rows for multi-row writes. */
    protected var multipleValues: RawValuesList? = null

    /**
     * List variable that supplies the values instead of the section body, resolved on the main
     * thread while local variables are still attached to the event.
     */
    private var valuesVariable: Variable<*>? = null

    /** Whether a nested `where` block is accepted. */
    protected open val supportsWhere: Boolean = false

    /**
     * Whether the values payload may contain multiple rows. Sections that reject it require exactly
     * one row from either value source.
     */
    protected open val supportsMultipleRows: Boolean = false

    /** Whether the operation needs values, from either the section body or a list variable. */
    protected open val requiresValues: Boolean = true

    protected var where: RawWhereClause? = null

    /**
     * Position of the table name expression for the matched pattern.
     *
     * The forms that read their values from a list variable place that expression before the table
     * name, so the table name moves back by one slot whenever [valuesExpressionIndex] matches.
     */
    protected open fun tableNameIndex(matchedPattern: Int): Int =
        if (valuesExpressionIndex(matchedPattern) >= 0) 1 else 0

    /**
     * Position of the expression that supplies the values from a list variable, or -1 when the
     * matched pattern reads its values from the section body instead.
     */
    protected open fun valuesExpressionIndex(matchedPattern: Int): Int = -1

    /** Position of the first expression after the table name, for example a limit or a primary key. */
    protected fun extraParamsIndex(matchedPattern: Int): Int = tableNameIndex(matchedPattern) + 1

    /**
     * Extracts the expressions specific to the matched pattern, after the table name has been read.
     *
     * @param expressions the expressions of the section, padded with nulls for omitted optional groups
     * @param matchedPattern the index of the matched pattern, for sections that register several
     */
    protected open fun extractExtraParams(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int
    ) {
    }

    protected open fun resolveExtraArguments(event: Event?): Any? = Unit

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult,
        sectionNode: SectionNode,
        triggerItems: List<TriggerItem?>
    ): Boolean {
        tableNameExpr = expressions[tableNameIndex(matchedPattern)] as Expression<String>
        extractExtraParams(expressions, matchedPattern)
        // The count clause is the last expression of every write pattern, so the target is the last
        // slot; it is null when the optional group was left out.
        affectedRowsVariable = try {
            AffectedRows.target(expressions.lastOrNull())
        } catch (error: IllegalArgumentException) {
            Skript.error(error.message ?: "Invalid affected row count target.")
            return false
        }
        // Every write is delayed, so Skript knows the lines after it run later. The `and wait` a script
        // may still write is accepted by the pattern and read by nobody.
        parser.hasDelayBefore = Kleenean.TRUE

        val valuesIndex = valuesExpressionIndex(matchedPattern)
        if (valuesIndex >= 0) {
            val valuesExpression = expressions[valuesIndex]
            if (valuesExpression !is Variable<*> || !valuesExpression.isList) {
                Skript.error("Write values must be stored in a list variable, for example {_values::*}.")
                return false
            }
            valuesVariable = valuesExpression
        }

        if (supportsWhere) {
            val rawWhereNode = sectionNode.find { WhereParser.isWhereSection(it) }
            if (rawWhereNode != null && rawWhereNode !is SectionNode) {
                Skript.error("The where clause must be a section.")
                return false
            }
            if (rawWhereNode != null) {
                where = try {
                    WhereParser.collectFromSection(rawWhereNode)
                } catch (error: IllegalArgumentException) {
                    Skript.error(error.message ?: "Invalid where section.")
                    return false
                }
                if (where == null) {
                    Skript.error("The where section cannot be empty.")
                    return false
                }
            }
        }

        if (!requiresValues) {
            // Nothing is read from the body except the where clause, so any other block would be
            // silently ignored, which is dangerous for a write that modifies every matching row.
            val unexpectedNode = sectionNode.find { !supportsWhere || !WhereParser.isWhereSection(it) }
            if (unexpectedNode != null) {
                Skript.error("This write section cannot contain the section '${unexpectedNode.key}'.")
                return false
            }
        }

        if (requiresValues) {
            val rawValuesNode = sectionNode.find { ValuesParser.isValuesSection(it) }
            if (rawValuesNode != null && rawValuesNode !is SectionNode) {
                Skript.error("The values clause must be a section.")
                return false
            }

            val valueNodes = if (rawValuesNode != null) {
                try {
                    ValuesParser.requireValuesHeader(rawValuesNode)
                } catch (error: IllegalArgumentException) {
                    Skript.error(error.message ?: "Invalid values section.")
                    return false
                }
                rawValuesNode.toList()
            } else if (supportsWhere) {
                // Without an explicit values block, collect body nodes other than `where` as values.
                sectionNode.filter { !WhereParser.isWhereSection(it) }
            } else {
                sectionNode.toList()
            }

            try {
                val collected = ValuesParser.collect(valueNodes, supportsMultipleRows)
                singleValues = collected?.first
                multipleValues = collected?.second
            } catch (error: IllegalArgumentException) {
                Skript.error(error.message ?: "Invalid values section.")
                return false
            }

            if (singleValues == null && multipleValues == null && valuesVariable == null) {
                Skript.error("Values section is required and cannot be empty.")
                return false
            }

            if (valuesVariable != null && (singleValues != null || multipleValues != null)) {
                Skript.error(
                    "This write section cannot take its values from both a list variable and a values block."
                )
                return false
            }
        }

        return true
    }

    override fun walk(event: Event?): TriggerItem? {
        if (event == null) return walk(event, false)
        if (this.trigger == null) return walk(event, false)
        DatabaseWork.clearErrorForStatement(event)
        // A refused write must not leave the previous affected row count in place.
        AffectedRows.clear(affectedRowsVariable, event)

        val database = ConnectionScope.resolve(event) ?: run {
            DatabaseWork.report(event, this, ConnectionScope.noConnectionMessage())
            return walk(event, false)
        }

        val tableName = tableNameExpr.getSingle(event) ?: run {
            DatabaseWork.report(event, this, "Table name is null.")
            return walk(event, false)
        }

        val table = database.tables[tableName] ?: run {
            val message = "Table '$tableName' not found."
            DatabaseWork.report(event, this, message)
            return walk(event, false)
        }

        val whereClause = try {
            where?.bind(table)?.resolve(event)
        } catch (error: Exception) {
            DatabaseWork.report(event, this, "Failed to parse where clause: ${error.message}")
            return walk(event, false)
        }

        // Resolved rows stay sparse so that an omitted column keeps its meaning: not part of this
        // statement. The one exception is a variable holding several rows, which the reader fills to
        // a common column set, because a single statement can only bind one column list.
        var resolvedSingle: Map<String, Any?>? = null
        var resolvedMultiple: List<Map<String, Any?>>? = null
        var reader: VariableValuesReader.ReadSession? = null
        try {
            val variable = valuesVariable
            if (variable != null) {
                if (supportsMultipleRows) {
                    reader = VariableValuesReader.begin(variable, table, event)
                } else {
                    val rows = VariableValuesReader.read(variable, table, event)
                    // Rejecting more than one row keeps this write a patch over a single row. The row
                    // itself is not padded, so a column the variable omits is left untouched.
                    require(rows.size == 1) {
                        "$variable holds ${rows.size} rows, but this write expects exactly one row of values."
                    }
                    resolvedSingle = rows.first()
                }
            } else {
                resolvedSingle = singleValues?.bind(table)?.resolve(event)
                resolvedMultiple = multipleValues?.bind(table)?.resolve(event)
            }
        } catch (error: Exception) {
            DatabaseWork.report(event, this, "Failed to parse write values: ${error.message}")
            return walk(event, false)
        }

        val extraArguments = try {
            resolveExtraArguments(event)
        } catch (error: Exception) {
            val message = "Failed to parse write arguments: ${error.message}"
            DatabaseWork.report(event, this, message)
            return walk(event, false)
        }

        if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) {
            DatabaseWork.report(event, this, "Database lifecycle is shutting down.")
            return walk(event, false)
        }

        if (DatabaseWork.skipInactiveTransaction(event, this)) {
            return walk(event, false)
        }
        val transaction = ConnectionScope.transaction(event)
        // Every write waits, whether or not it says `and wait`: the trigger carries on after the change
        // has happened, so the order a script reads is the order it wrote, and every failure is in
        // `last database error` as well as through Skript's runtime error channel. `and wait` is still
        // accepted, and does nothing, the way it has always been on a read.
        val batchReader = reader
        return DatabaseWork.run(
            event = event,
            continuation = next,
            prepare = batchReader?.takeUnless { it.background }?.let { it::advance },
            query = {
                batchReader?.prepareBackground()
                DatabaseWork.withQueries(database, transaction) { queries ->
                    executeWrite(
                        queries,
                        table,
                        resolvedSingle,
                        batchReader?.rows ?: resolvedMultiple,
                        whereClause,
                        extraArguments
                    )
                }
            },
            deliver = { result -> AffectedRows.write(affectedRowsVariable, event, result) },
            onFailure = { failure -> this.error("Write failed: ${failure.message}") }
        )
    }

    protected abstract suspend fun executeWrite(
        queries: Queries,
        table: Table,
        singleValues: Map<String, Any?>?,
        multipleValues: List<Map<String, Any?>>?,
        whereClause: WhereClause?,
        extraArguments: Any?
    ): WriteResult
}
