package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.effects.Delay
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
import io.github.heyhey123.skriptorm.skript.utils.ConnectionScope
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.RawWhereClause
import io.github.heyhey123.skriptorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.skriptorm.skript.utils.SkriptLocalVariables
import io.github.heyhey123.skriptorm.skript.utils.VariableModifier
import io.github.heyhey123.skriptorm.skript.utils.WhereParser
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.utils.SyncDispatcher
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

    protected open fun resolveExtraArguments(event: Event?): Any? = Unit

    /**
     * Reports a refusal and clears the result variable, because a read that did not run must not leave
     * the previous result behind. [DatabaseWork.refuseRead] owns the rule and the reasoning.
     */
    private fun refuse(event: Event, message: String) {
        DatabaseWork.refuseRead(event, this, message, resultVar)
    }

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return walk(event, false)
        if (this.trigger == null) return walk(event, false)
        DatabaseWork.clearErrorForStatement(actualEvent)

        val database = ConnectionScope.resolve(event) ?: run {
            refuse(actualEvent, ConnectionScope.noConnectionMessage())
            return walk(event, false)
        }

        val tableName = tableNameExpr.getSingle(event) ?: run {
            refuse(actualEvent, "Table name is null.")
            return walk(event, false)
        }

        val table = database.tables[tableName] ?: run {
            val message = "Table '$tableName' not found."
            refuse(actualEvent, message)
            return walk(event, false)
        }

        val whereClause = where?.let { raw ->
            try {
                raw.bind(table).resolve(event)
            } catch (e: Exception) {
                val message = "Failed to parse where clause: ${e.message}"
                refuse(actualEvent, message)
                return walk(event, false)
            }
        }

        val extraArguments = try {
            resolveExtraArguments(event)
        } catch (e: Exception) {
            val message = "Failed to parse query arguments: ${e.message}"
            refuse(actualEvent, message)
            return walk(event, false)
        }

        if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) {
            refuse(actualEvent, "Database lifecycle is shutting down.")
            return walk(actualEvent, false)
        }

        if (DatabaseWork.skipInactiveTransaction(actualEvent, this)) {
            // The statement was skipped by the transaction it belongs to and never ran, so the variable
            // is cleared like any other refusal. The transaction's own failure stays in the error slot.
            VariableModifier.clear(resultVar, actualEvent)
            return walk(actualEvent, false)
        }
        val transaction = ConnectionScope.transaction(actualEvent)

        val continuation = next
        // Store the continuation in order to resume after the query is complete
        // Continuation means the rest of the script after this section, which will be executed after the query is done
        val localVariables = SkriptLocalVariables.remove(actualEvent)
        Delay.addDelayedEvent(actualEvent)

        SkriptOrm.ioScope.launch {
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
                if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) return@withContext
                try {
                    if (localVariables != null) {
                        SkriptLocalVariables.restore(actualEvent, localVariables)
                    }

                    val queryFailure = failure
                    if (queryFailure != null) {
                        VariableModifier.clear(resultVar, actualEvent)
                        DatabaseWork.recordFailure(actualEvent, queryFailure)
                        this@SecSelectBase.error("Query failed: ${queryFailure.message}")
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
