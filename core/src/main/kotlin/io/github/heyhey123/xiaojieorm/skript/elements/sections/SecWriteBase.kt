package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.XiaojieOrm
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.skript.utils.*
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event

abstract class SecWriteBase : Section() {

    protected lateinit var tableNameExpr: Expression<String>

    /**
     * Whether the continuation waits for this write. Waiting preserves the event continuation and
     * exposes failures through `last database error`; fire-and-forget writes continue immediately
     * and can only report later failures to the server log.
     */
    protected var waitFlag: Boolean = false

    /** Values for single-row writes. */
    protected var singleValues: RawValues? = null

    /** Rows for multi-row writes. */
    protected var multipleValues: RawValuesList? = null

    /** Whether a nested `where` block is accepted. */
    protected open val supportsWhere: Boolean = false

    /** Whether the values payload may contain multiple rows. */
    protected open val supportsMultipleRows: Boolean = false

    /** Whether a non-empty values payload is required. */
    protected open val requiresValues: Boolean = true

    protected var where: RawWhereClause? = null

    protected abstract val tableNameIndex: Int

    /**
     * Override this method to extract any extra parameters from the expressions array.
     * The default implementation does nothing.
     * This method is called during the initialization of the section, after the table name expression has been extracted.
     * You can use this method to extract any additional parameters that your section may require, such as limit, offset, or other options.
     * The extracted parameters can be stored in member variables for later use in the executeWrite method.
     * @param expressions The array of expressions passed to the section. You can extract any additional parameters from this array.
     */
    protected open fun extractExtraParams(expressions: Array<out Expression<*>?>) {}

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
        tableNameExpr = expressions[tableNameIndex] as Expression<String>
        extractExtraParams(expressions)
        waitFlag = parseResult.hasTag("wait")
        if (waitFlag) {
            parser.hasDelayBefore = Kleenean.TRUE
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

            if (singleValues == null && multipleValues == null) {
                Skript.error("Values section is required and cannot be empty.")
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

        val resolvedSingle: Map<String, Any?>?
        val resolvedMultiple: List<Map<String, Any?>>?
        try {
            resolvedSingle = singleValues?.bind(table)?.resolve(event)
            resolvedMultiple = multipleValues?.bind(table)?.resolve(event)
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
