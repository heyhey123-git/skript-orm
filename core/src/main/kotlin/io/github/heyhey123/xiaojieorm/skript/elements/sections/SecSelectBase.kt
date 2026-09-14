package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.lang.*
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.XiaojieOrm
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import io.github.heyhey123.xiaojieorm.skript.utils.RawWhereClause
import io.github.heyhey123.xiaojieorm.skript.utils.WhereParser
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
import kotlinx.coroutines.launch
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
        resultVar = expressions[resultVarIndex] as Variable<Any>
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

        XiaojieOrm.ioScope.launch {
            try {
                executeQuery(database, table, whereClause, extraArguments, event)
            } catch (e: Throwable) {
                launch(SyncDispatcher) {
                    ErrorPrinter.printErrorMessageWithDetail(trigger, "Query failed: ${e.message}")
                }
            } finally {
                if (waitFlag) {
                    launch(SyncDispatcher) { walk(event, false) }
                }
            }
        }

        return if (waitFlag) null else walk(event, false)
    }

    /**
     * Execute the query logic in subclasses.
     * This method is called in IO dispatcher, when [walk] is invoked.
     *
     * @param database the current database
     * @param table the table to query
     * @param whereClause the where clause, can be null
     * @param event the current event, can be null
     */
    protected abstract suspend fun executeQuery(
        database: Database,
        table: Table,
        whereClause: WhereClause?,
        extraArguments: Any?,
        event: Event?
    )
}

