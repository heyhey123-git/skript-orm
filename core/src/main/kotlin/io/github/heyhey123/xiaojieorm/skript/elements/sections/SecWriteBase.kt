package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.skript.lang.Variable
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.XiaojieOrm
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import io.github.heyhey123.xiaojieorm.skript.utils.RawValues
import io.github.heyhey123.xiaojieorm.skript.utils.RawValuesList
import io.github.heyhey123.xiaojieorm.skript.utils.RawWhereClause
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptLocalVariables
import io.github.heyhey123.xiaojieorm.skript.utils.ValuesParser
import io.github.heyhey123.xiaojieorm.skript.utils.VariableValuesReader
import io.github.heyhey123.xiaojieorm.skript.utils.WhereParser
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event

/**
 * Shared behaviour of write sections.
 *
 * A write either reads its values from the section body, from a list variable given as an expression,
 * or has no values at all, as a delete does. The first two sources are mutually exclusive, and both
 * are resolved on the main thread into the same rows before the operation is dispatched.
 *
 * ## Missing columns
 *
 * Both sources resolve to a map keyed by column name, and both may leave a column out. They do not
 * agree on what leaving a column out means:
 *
 * - The body source can tell an omitted key from a key written as `null`, because the latter resolves
 *   to a present entry holding null and is bound as SQL NULL.
 * - A list variable cannot. Skript deletes a key when it is set to null, so "not supplied" and
 *   "supplied as NULL" are both just an absent key.
 *
 * An absent key is therefore never read as NULL. The column simply does not reach the statement,
 * which makes every write except `insert many` a patch: an omitted column is not part of the `SET`
 * or `ON DUPLICATE KEY UPDATE` list and keeps its stored value, while an insert omits it so that the
 * database default applies. [VariableValuesReader] documents the full contract, including why
 * `insert many` has to fill a common column set instead.
 *
 * Reading an absent key as null would only be meaningful for an explicit full-row replacement
 * operation. That is deliberately not offered: the same input would otherwise clear every column a
 * dynamic variable happens to miss, and an unknown column name is the only mistake this reader can
 * reject, while a forgotten column passes silently.
 */
abstract class SecWriteBase : Section() {

    protected lateinit var tableNameExpr: Expression<String>

    /**
     * Whether the continuation waits for this write. Waiting preserves the event continuation and
     * exposes failures through `last database error`; fire-and-forget writes continue immediately
     * and can only report later failures to the server log.
     */
    protected var waitFlag: Boolean = false

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
        waitFlag = parseResult.hasTag("wait")
        if (waitFlag) {
            parser.hasDelayBefore = Kleenean.TRUE
        }

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
                // When only where is supported, we can assume that all other sections are values sections.
                // In that case, exclude the where section from the value nodes is required, otherwise the where section will be treated as a values section and cause an error.
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
        val trigger = this.trigger ?: return walk(event, false)
        if (event != null) SkriptDatabaseErrors.clear(event)

        val database = Database.current ?: run {
            if (event != null) SkriptDatabaseErrors.set(event, "No database connected.")
            ErrorPrinter.printErrorMessageWithDetail(trigger, "No database connected.")
            return walk(event, false)
        }

        val tableName = tableNameExpr.getSingle(event) ?: run {
            if (event != null) SkriptDatabaseErrors.set(event, "Table name is null.")
            ErrorPrinter.printErrorMessageWithDetail(trigger, "Table name is null.")
            return walk(event, false)
        }

        val table = database.tables[tableName] ?: run {
            val message = "Table '$tableName' not found."
            if (event != null) SkriptDatabaseErrors.set(event, message)
            ErrorPrinter.printErrorMessageWithDetail(trigger, message)
            return walk(event, false)
        }

        val whereClause = try {
            where?.bind(table)?.resolve(event)
        } catch (error: Exception) {
            val message = "Failed to parse where clause: ${error.message}"
            if (event != null) SkriptDatabaseErrors.set(event, message)
            ErrorPrinter.printErrorMessageWithDetail(
                trigger,
                message
            )
            return walk(event, false)
        }

        // Resolved rows stay sparse so that an omitted column keeps its meaning: not part of this
        // statement. The one exception is a variable holding several rows, which the reader fills to
        // a common column set, because a single statement can only bind one column list.
        val resolvedSingle: Map<String, Any?>?
        val resolvedMultiple: List<Map<String, Any?>>?
        try {
            val variable = valuesVariable
            if (variable != null) {
                val rows = VariableValuesReader.read(variable, table, event)
                if (supportsMultipleRows) {
                    resolvedSingle = null
                    resolvedMultiple = rows
                } else {
                    // Rejecting more than one row keeps this write a patch over a single row. The row
                    // itself is not padded, so a column the variable omits is left untouched.
                    require(rows.size == 1) {
                        "$variable holds ${rows.size} rows, but this write expects exactly one row of values."
                    }
                    resolvedSingle = rows.first()
                    resolvedMultiple = null
                }
            } else {
                resolvedSingle = singleValues?.bind(table)?.resolve(event)
                resolvedMultiple = multipleValues?.bind(table)?.resolve(event)
            }
        } catch (error: Exception) {
            val message = "Failed to parse write values: ${error.message}"
            if (event != null) SkriptDatabaseErrors.set(event, message)
            ErrorPrinter.printErrorMessageWithDetail(
                trigger,
                message
            )
            return walk(event, false)
        }

        val extraArguments = try {
            resolveExtraArguments(event)
        } catch (error: Exception) {
            val message = "Failed to parse write arguments: ${error.message}"
            if (event != null) SkriptDatabaseErrors.set(event, message)
            ErrorPrinter.printErrorMessageWithDetail(trigger, message)
            return walk(event, false)
        }

        if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) {
            if (event != null) SkriptDatabaseErrors.set(event, "Database lifecycle is shutting down.")
            ErrorPrinter.printErrorMessageWithDetail(trigger, "Database lifecycle is shutting down.")
            return walk(event, false)
        }

        val continuation = if (waitFlag) next else null
        val localVariables = if (waitFlag && event != null) {
            SkriptLocalVariables.remove(event)
        } else {
            null
        }
        if (waitFlag && event != null) {
            Delay.addDelayedEvent(event)
        }

        XiaojieOrm.ioScope.launch {
            var failure: Throwable? = null
            try {
                database.withQueries { queries ->
                    executeWrite(queries, table, resolvedSingle, resolvedMultiple, whereClause, extraArguments)
                }
            } catch (_: CancellationException) {
                return@launch
            } catch (error: Throwable) {
                failure = error
            }

            withContext(NonCancellable + SyncDispatcher) {
                if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) return@withContext

                if (waitFlag && event != null) {
                    if (failure != null) {
                        SkriptDatabaseErrors.set(event, failure)
                    } else {
                        SkriptDatabaseErrors.clear(event)
                    }
                }
                failure?.let {
                    ErrorPrinter.printErrorMessageWithDetail(trigger, "Write failed: ${it.message}")
                }
                if (waitFlag) {
                    try {
                        if (event != null && localVariables != null) {
                            SkriptLocalVariables.restore(event, localVariables)
                        }
                        if (event != null) {
                            walk(continuation, event)
                        }
                    } finally {
                        if (event != null) SkriptLocalVariables.clear(event)
                    }
                }
            }
        }

        return if (waitFlag) null else walk(event, false)
    }

    protected abstract suspend fun executeWrite(
        queries: Queries,
        table: Table,
        singleValues: Map<String, Any?>?,
        multipleValues: List<Map<String, Any?>>?,
        whereClause: WhereClause?,
        extraArguments: Any?
    )
}
