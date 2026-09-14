package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.lang.*
import ch.njol.skript.effects.Delay
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.XiaojieOrm
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import io.github.heyhey123.xiaojieorm.skript.utils.RawWhereClause
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptLocalVariables
import io.github.heyhey123.xiaojieorm.skript.utils.VariableModifier
import io.github.heyhey123.xiaojieorm.skript.utils.WhereParser
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event

abstract class SecSelectBase : Section() {

    protected lateinit var tableNameExpr: Expression<String>

    protected lateinit var resultVar: Variable<Any>

    protected var where: RawWhereClause? = null

    protected var waitFlag: Boolean = false

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

        if (parseResult.hasTag("where")) {
            where = WhereParser.collect(sectionNode, parseResult.hasTag("any"), parseResult.hasTag("neg"))
            if (where == null) {
                Skript.error("The where section cannot be empty when using where clause.")
                return false
            }
        }

        waitFlag = parseResult.hasTag("wait")
        if (!waitFlag && resultVar.isLocal) {
            Skript.error(
                "A non-waiting database query cannot store its result in a local variable. " +
                    "Add 'and wait' or use a global variable."
            )
            return false
        }
        if (waitFlag) {
            parser.hasDelayBefore = Kleenean.TRUE
        }
        return true
    }

    protected open fun resolveExtraArguments(event: Event?, trigger: Trigger): Any? = Unit

    override fun walk(event: Event?): TriggerItem? {
        val trigger = first?.trigger ?: return walk(event, false)

        val database = Database.current ?: run {
            ErrorPrinter.printErrorMessageWithDetail(trigger, "No database connected.")
            return walk(event, false)
        }

        val tableName = tableNameExpr.getSingle(event) ?: run {
            ErrorPrinter.printErrorMessageWithDetail(trigger, "Table name is null.")
            return walk(event, false)
        }

        val table = database.tables[tableName] ?: run {
            ErrorPrinter.printErrorMessageWithDetail(trigger, "Table '$tableName' not found.")
            return walk(event, false)
        }

        val whereClause = where?.let { raw ->
            try {
                raw.bind(table).resolve(event)
            } catch (e: Exception) {
                ErrorPrinter.printErrorMessageWithDetail(trigger, "Failed to parse where clause: ${e.message}")
                return walk(event, false)
            }
        }

        val extraArguments = try {
            resolveExtraArguments(event, trigger)
        } catch (e: Exception) {
            ErrorPrinter.printErrorMessageWithDetail(trigger, "Failed to parse query arguments: ${e.message}")
            return walk(event, false)
        }

        val localVariables = if (waitFlag && event != null) {
            SkriptLocalVariables.remove(event)
        } else {
            null
        }
        if (waitFlag && event != null) {
            Delay.addDelayedEvent(event)
        }

        XiaojieOrm.ioScope.launch {
            var queryResult: Map<String, Any?>? = null
            var failure: Throwable? = null
            try {
                queryResult = executeQuery(database, table, whereClause, extraArguments)
            } catch (e: Throwable) {
                failure = e
            } finally {
                withContext(NonCancellable + SyncDispatcher) {
                    try {
                        if (waitFlag && event != null && localVariables != null) {
                            SkriptLocalVariables.restore(event, localVariables)
                        }

                        val queryFailure = failure
                        if (queryFailure != null) {
                            VariableModifier.clear(resultVar, event)
                            ErrorPrinter.printErrorMessageWithDetail(trigger, "Query failed: ${queryFailure.message}")
                        } else {
                            VariableModifier.writeMap(resultVar, event, checkNotNull(queryResult))
                        }

                        if (waitFlag) {
                            walk(event, false)
                        }
                    } finally {
                        if (waitFlag && event != null) {
                            SkriptLocalVariables.clear(event)
                        }
                    }
                }
            }
        }

        return if (waitFlag) null else walk(event, false)
    }

    /**
     * Executes the database query on the IO dispatcher and returns the keyed snapshot
     * that will be written to the Skript list variable on the main thread.
     *
     * Multi-row implementations use keys in the form `rowIndex::columnName` and
     * reserve `rowIndex::__index` as a non-null row-presence marker.
     */
    protected abstract suspend fun executeQuery(
        database: Database,
        table: Table,
        whereClause: WhereClause?,
        extraArguments: Any?
    ): Map<String, Any?>
}

