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
     * If true, the section will wait for the write operation to complete before continuing.
     * If false, the section will continue immediately after starting the write operation.
     * It's recommended to set this to true for most cases,
     * as it allows for error handling and ensures that the write operation has completed before proceeding.
     * In order to follow the Skript error handling style, the error usually will only be optionally handled,
     * so the most of the time, people won't want to wait for the write operation to complete,
     * also they won't want to handle the error explicitly, so the default value is false.
     * In "select" operations, the wait is always true, because the result of the select operation is usually needed immediately after the operation.
     */
    protected var waitFlag: Boolean = false

    // 单行值（用于 INSERT ONE / UPDATE）
    protected var singleValues: RawValues? = null

    // 多行值（用于 INSERT MANY）
    protected var multipleValues: RawValuesList? = null

    /**
     * Whether the section supports a where clause. If true, the section will parse a where clause from the section node.
     */
    protected open val supportsWhere: Boolean = false

    /**
     * Whether the section supports multiple rows of values. If true, the section will parse multiple rows of values from the section node.
     */
    protected open val supportsMultipleRows: Boolean = false

    /**
     * Whether the section requires values to be provided. If true, the section will parse values from the section node and will throw an error if no values are provided.
     */
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
            val rawWhereNode = sectionNode.find { it.key?.startsWith("where") == true }
            if (rawWhereNode != null && rawWhereNode !is SectionNode) {
                Skript.error("The where clause must be a section.")
                return false
            }
            if (rawWhereNode != null) {
                where = WhereParser.collectFromSection(rawWhereNode)
                if (where == null) {
                    Skript.error("The where section cannot be empty.")
                    return false
                }
            }
        }

        if (requiresValues) {
            val rawValuesNode = sectionNode.find { it.key?.startsWith("values") == true }
            if (rawValuesNode != null && rawValuesNode !is SectionNode) {
                Skript.error("The values clause must be a section.")
                return false
            }
            val targetNode = rawValuesNode ?: sectionNode

            try {
                if (supportsMultipleRows) {
                    val (single, multiple) = ValuesParser.collect(targetNode)
                    singleValues = single
                    multipleValues = multiple
                } else {
                    singleValues = ValuesParser.collectSingle(targetNode)
                }
            } catch (error: IllegalArgumentException) {
                Skript.error(error.message ?: "Invalid values section.")
                return false
            }

            if ((singleValues == null || singleValues?.values?.isEmpty() == true) &&
                (multipleValues == null || multipleValues?.valuesList?.isEmpty() == true)) {
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
