package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Trigger
import ch.njol.skript.lang.TriggerItem
import ch.njol.skript.lang.Variable
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.XiaojieOrm
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.skript.utils.ConnectionScope
import io.github.heyhey123.xiaojieorm.skript.utils.DatabaseWork
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
     * Whether this select section accepts a `where` block.
     * Select sections that filter by something else, such as the primary key, must disable it,
     * otherwise a `where` block would be parsed and then silently ignored.
     */
    protected open val supportsWhere: Boolean = true

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

        val unsupportedNode = if (supportsWhere) {
            sectionNode.find { !WhereParser.isWhereSection(it) }
        } else {
            sectionNode.firstOrNull()
        }
        if (unsupportedNode != null) {
            Skript.error("This select section cannot contain the section '${unsupportedNode.key}'.")
            return false
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

        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    protected open fun resolveExtraArguments(event: Event?, trigger: Trigger): Any? = Unit

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return walk(event, false)
        val trigger = this.trigger ?: return walk(event, false)
        DatabaseWork.clearErrorForStatement(actualEvent)

        val database = ConnectionScope.resolve(event) ?: run {
            DatabaseWork.report(actualEvent, trigger, ConnectionScope.noConnectionMessage())
            return walk(event, false)
        }

        val tableName = tableNameExpr.getSingle(event) ?: run {
            DatabaseWork.report(actualEvent, trigger, "Table name is null.")
            return walk(event, false)
        }

        val table = database.tables[tableName] ?: run {
            val message = "Table '$tableName' not found."
            DatabaseWork.report(actualEvent, trigger, message)
            return walk(event, false)
        }

        val whereClause = where?.let { raw ->
            try {
                raw.bind(table).resolve(event)
            } catch (e: Exception) {
                val message = "Failed to parse where clause: ${e.message}"
                DatabaseWork.report(actualEvent, trigger, message)
                return walk(event, false)
            }
        }

        val extraArguments = try {
            resolveExtraArguments(event, trigger)
        } catch (e: Exception) {
            val message = "Failed to parse query arguments: ${e.message}"
            DatabaseWork.report(actualEvent, trigger, message)
            return walk(event, false)
        }

        if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) {
            DatabaseWork.report(actualEvent, trigger, "Database lifecycle is shutting down.")
            return walk(actualEvent, false)
        }

        if (DatabaseWork.skipInactiveTransaction(actualEvent, trigger)) {
            return walk(actualEvent, false)
        }
        val transaction = ConnectionScope.transaction(actualEvent)

        val continuation = next
        // Store the continuation in order to resume after the query is complete
        // Continuation means the rest of the script after this section, which will be executed after the query is done
        val localVariables = SkriptLocalVariables.remove(actualEvent)
        Delay.addDelayedEvent(actualEvent)

        XiaojieOrm.ioScope.launch {
            var queryResult: Map<String, Any?>? = null
            var failure: Throwable? = null
            try {
                queryResult = DatabaseWork.withQueries(database, transaction) { queries ->
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
     * Single-row implementations use the column name as the key, so a row of the table `users`
     * is stored as `{_user::name}`, `{_user::age}` and so on.
     *
     * Multi-row implementations always use keys in the form `rowIndex::columnName`, where the row
     * index starts at one and is present even when the result holds a single row.
     *
     * SQL NULL values leave their corresponding result keys unset; no ORM metadata is inserted.
     */
    protected abstract suspend fun executeQuery(
        queries: Queries,
        table: Table,
        whereClause: WhereClause?,
        extraArguments: Any?
    ): Map<String, Any?>
}
