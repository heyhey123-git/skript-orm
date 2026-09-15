package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.lang.*
import ch.njol.skript.effects.Delay
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.XiaojieOrm
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import io.github.heyhey123.xiaojieorm.skript.utils.RawWhereClause
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptLocalVariables
import io.github.heyhey123.xiaojieorm.skript.utils.VariableModifier
import io.github.heyhey123.xiaojieorm.skript.utils.WhereParser
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event

abstract class SecSelectBase : Section() {

    protected lateinit var tableNameExpr: Expression<String>

    protected lateinit var resultVar: Variable<Any>

    protected var where: RawWhereClause? = null

    /**
     * The index of table name expression in expressions array.
     *
     * @return
     */
    protected abstract val tableNameIndex: Int

    /**
     * The index of result variable expression in expressions array.
     */
    protected abstract val resultVarIndex: Int

    /**
     * Extract extra parameters from expressions array for subclasses,
     * automatically invoke when init.
     *
     * @param expressions
     */
    protected open fun extractExtraParams(expressions: Array<out Expression<*>?>) {}

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
        val resultExpression = expressions[resultVarIndex]
        if (resultExpression !is Variable<*> || !resultExpression.isList) {
            Skript.error("Database query results must be stored in a list variable, for example {_rows::*}.")
            return false
        }
        resultVar = resultExpression as Variable<Any>
        extractExtraParams(expressions)

        val unsupportedNode = sectionNode.find { it.key?.startsWith("where") == false }
        if (unsupportedNode != null) {
            Skript.error("A select section may only contain a 'where' section.")
            return false
        }

        val rawWhereNode = sectionNode.find { it.key?.startsWith("where") == true }
        if (rawWhereNode != null && rawWhereNode !is SectionNode) {
            Skript.error("The where clause must be a section.")
            return false
        }
        val whereNode = rawWhereNode as? SectionNode
        if (whereNode != null) {
            where = WhereParser.collectFromSection(whereNode)
            if (where == null) {
                Skript.error("The where section cannot be empty.")
                return false
            }
        }

        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    protected open fun resolveExtraArguments(event: Event?, trigger: Trigger): Any? = Unit

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return walk(event, false)
        val trigger = this.trigger ?: return walk(event, false)
        SkriptDatabaseErrors.clear(actualEvent)

        val database = Database.current ?: run {
            SkriptDatabaseErrors.set(actualEvent, "No database connected.")
            ErrorPrinter.printErrorMessageWithDetail(trigger, "No database connected.")
            return walk(event, false)
        }

        val tableName = tableNameExpr.getSingle(event) ?: run {
            SkriptDatabaseErrors.set(actualEvent, "Table name is null.")
            ErrorPrinter.printErrorMessageWithDetail(trigger, "Table name is null.")
            return walk(event, false)
        }

        val table = database.tables[tableName] ?: run {
            val message = "Table '$tableName' not found."
            SkriptDatabaseErrors.set(actualEvent, message)
            ErrorPrinter.printErrorMessageWithDetail(trigger, message)
            return walk(event, false)
        }

        val whereClause = where?.let { raw ->
            try {
                raw.bind(table).resolve(event)
            } catch (e: Exception) {
                val message = "Failed to parse where clause: ${e.message}"
                SkriptDatabaseErrors.set(actualEvent, message)
                ErrorPrinter.printErrorMessageWithDetail(trigger, message)
                return walk(event, false)
            }
        }

        val extraArguments = try {
            resolveExtraArguments(event, trigger)
        } catch (e: Exception) {
            val message = "Failed to parse query arguments: ${e.message}"
            SkriptDatabaseErrors.set(actualEvent, message)
            ErrorPrinter.printErrorMessageWithDetail(trigger, message)
            return walk(event, false)
        }

        if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) {
            SkriptDatabaseErrors.set(actualEvent, "Database lifecycle is shutting down.")
            ErrorPrinter.printErrorMessageWithDetail(trigger, "Database lifecycle is shutting down.")
            return walk(actualEvent, false)
        }

        val continuation = next
        // Store the continuation in order to resume after the query is complete
        // Continuation means the rest of the script after this section, which will be executed after the query is done
        val localVariables = SkriptLocalVariables.remove(actualEvent)
        Delay.addDelayedEvent(actualEvent)

        XiaojieOrm.ioScope.launch {
            var queryResult: Map<String, Any?>? = null
            var failure: Throwable? = null
            try {
                queryResult = database.withQueries { queries ->
                    executeQuery(queries, table, whereClause, extraArguments)
                }
            } catch (_: CancellationException) {
                return@launch
            } catch (error: Throwable) {
                failure = error
            }

            withContext(NonCancellable + SyncDispatcher) {
                if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) return@withContext
                try {
                    if (localVariables != null) {
                        SkriptLocalVariables.restore(actualEvent, localVariables)
                    }

                    val queryFailure = failure
                    if (queryFailure != null) {
                        VariableModifier.clear(resultVar, actualEvent)
                        SkriptDatabaseErrors.set(actualEvent, queryFailure)
                        ErrorPrinter.printErrorMessageWithDetail(trigger, "Query failed: ${queryFailure.message}")
                    } else {
                        SkriptDatabaseErrors.clear(actualEvent)
                        VariableModifier.writeMap(resultVar, actualEvent, checkNotNull(queryResult))
                    }

                    walk(continuation, actualEvent)
                } finally {
                    SkriptLocalVariables.clear(actualEvent)
                }
            }
        }

        return null
    }

    /**
     * Executes the database query on the IO dispatcher and returns the keyed snapshot
     * that will be written to the Skript list variable on the main thread.
     *
     * Multi-row implementations use keys in the form `rowIndex::columnName`.
     * SQL NULL values leave their corresponding result keys unset; no ORM metadata is inserted.
     */
    protected abstract suspend fun executeQuery(
        queries: Queries,
        table: Table,
        whereClause: WhereClause?,
        extraArguments: Any?
    ): Map<String, Any?>
}

