package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import ch.njol.skript.variables.Variables
import io.github.heyhey123.xiaojieorm.XiaojieOrm
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.skript.utils.*
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event

abstract class SecWriteBase : Section() {

    protected lateinit var tableNameExpr: Expression<String>

    protected var waitFlag: Boolean = false

    // 单行值（用于 INSERT ONE / UPDATE）
    protected var singleValues: RawValues? = null

    // 多行值（用于 INSERT MANY）
    protected var multipleValues: RawValuesList? = null

    protected open val supportsWhere: Boolean = false

    protected open val supportsMultipleRows: Boolean = false

    protected var where: RawWhereClause? = null

    protected abstract val tableNameIndex: Int

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
        waitFlag = parseResult.hasTag("wait")
        if (waitFlag) {
            parser.hasDelayBefore = Kleenean.TRUE
        }

        // 解析 WHERE 子句（如果支持）
        if (supportsWhere) {
            val whereNode = sectionNode.find { it.key?.startsWith("where") == true } as? SectionNode
            if (whereNode != null) {
                val negTag = whereNode.key?.contains("no") == true || whereNode.key?.contains("not") == true
                val anyTag = whereNode.key?.contains("any") == true
                where = WhereParser.collect(whereNode, anyTag, negTag)
            }
        }

        // 解析 VALUES
        val valuesNode = sectionNode.find { it.key?.startsWith("values") == true } as? SectionNode

        // 确定要解析的目标节点：有 values: 块就用它，否则用整个 sectionNode
        val targetNode = valuesNode ?: sectionNode

        if (supportsMultipleRows) {
            val (single, multiple) = ValuesParser.collect(targetNode)
            singleValues = single
            multipleValues = multiple
        } else {
            singleValues = ValuesParser.collectSingle(targetNode)
        }

        if (singleValues == null && multipleValues == null) {
            Skript.error("Values section is required and cannot be empty.")
            return false
        }

        return true
    }


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

        // 解析 WHERE
        val whereClause = where?.bind(table)?.resolve(event)

        // 解析 VALUES
        val resolvedSingle = singleValues?.bind(table)?.resolve(event)
        val resolvedMultiple = multipleValues?.bind(table)?.resolve(event)

        XiaojieOrm.ioScope.launch {
            try {
                executeWrite(database, table, resolvedSingle, resolvedMultiple, whereClause, event)
            } catch (e: Throwable) {
                // Make sure the error message will be printed instantly, because the main thread may continue to execute other code and the error message may be delayed.
                ErrorPrinter.printErrorMessageWithDetail(trigger, "Write failed: ${e.message}")
            } finally {
                if (waitFlag) {
                    launch(SyncDispatcher) { walk(event, false) }
                }
            }
        }

        return if (waitFlag) null else walk(event, false)
    }

    protected abstract suspend fun executeWrite(
        database: Database,
        table: Table,
        singleValues: Map<String, Any?>?,
        multipleValues: List<Map<String, Any?>>?,
        whereClause: WhereClause?,
        event: Event?
    )
}
