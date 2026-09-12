package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Example
import ch.njol.skript.doc.Name
import ch.njol.skript.lang.*
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import io.github.heyhey123.xiaojieorm.skript.utils.RawWhereClause
import io.github.heyhey123.xiaojieorm.skript.utils.VariableModifier
import io.github.heyhey123.xiaojieorm.skript.utils.WhereParser
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.event.Event

@Name("Select One")
@Description("Select one entity from a table and store the result in a variable.")
@Example(
    """
connect to database "MySQL":
    url: "jdbc:mysql://localhost:3306/mydb"
    username: "root"
    password: "123456"
select one from table "users" and store the result in {_user::*} and wait where any:
    name = "Alice"
    age > 25
disconnect from database
"""
)

class SecSelectOne : Section() {

    companion object {
        init {
            Skript.registerSection(
                SecSelectOne::class.java,
                "select one [entity] from [table] %string% [and] store [the] [result] in %objects% [wait:and wait] [where:where (any:[neg:no] any|all:[neg:not] all)]"
            )
        }
    }

    private lateinit var tableNameExpr: Expression<String>

    private lateinit var resultVar: Variable<Any>

    private var where: RawWhereClause? = null

    private var waitFlag: Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean,
        parseResult: SkriptParser.ParseResult,
        sectionNode: SectionNode,
        triggerItems: List<TriggerItem?>
    ): Boolean {
        tableNameExpr = expressions[0] as Expression<String>
        resultVar = expressions[1] as Variable<Any>
        val whereFlag = parseResult.hasTag("where")
        val anyFlag = parseResult.hasTag("any")
        val negFlag = parseResult.hasTag("neg")
        if (whereFlag) {
            where = WhereParser.collect(sectionNode, anyFlag, negFlag)

            if (where == null) {
                Skript.error("The where section cannot be empty when using where clause.")
                return false
            }
        }

        waitFlag = parseResult.hasTag("wait")

        return true
    }

    override fun walk(event: Event?): TriggerItem? {
        val firstLine = first!!.trigger!!

        val database = Database.current
        if (database == null) {
            ErrorPrinter.printErrorMessageWithDetail(
                firstLine,
                "No database connected when executing select one section."
            )
            return walk(event, false)
        }

        val tableName = tableNameExpr.getSingle(event)
        if (tableName == null) {
            ErrorPrinter.printErrorMessageWithDetail(
                firstLine,
                "Table name is null in select one section."
            )
            return walk(event, false)
        }

        val table = Database.tables[tableName]
        if (table == null) {
            ErrorPrinter.printErrorMessageWithDetail(
                firstLine,
                "Table with name '$tableName' not found in select one section."
            )
            return walk(event, false)
        }

        val whereClause = where?.let { raw ->
            try {
                raw.bind(table).resolve(event)
            } catch (e: Exception) {
                ErrorPrinter.printErrorMessageWithDetail(
                    firstLine,
                    "Failed to parse where clause in select one section: ${e.message}"
                )
                return walk(event, false)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cursor = database.queries!!.selectMany(whereClause).execute(table).cursor
                val result = mutableMapOf<Int, Any?>()
                val columns = table.columns.values
                while (cursor.next()) {
                    for ((index, column) in columns.withIndex()) {
                        val value = cursor.get(column.name, column.type)
                        result[index + 1] = value
                    }
                }

                VariableModifier.writeMap(resultVar, event, result.mapKeys { it.key.toString() })

            } catch (e: Throwable) {
                ErrorPrinter.printErrorMessageWithDetail(
                    firstLine,
                    "Failed to execute select one query in select one section: ${e.message}"
                )
            } finally {
                if (waitFlag) {
                    launch(SyncDispatcher) {
                        walk(event, false) // resume the main thread
                    }
                }
            }
        }

        return if (waitFlag) null else walk(event, false)
    }

    override fun toString(event: Event?, debug: Boolean): String = buildString {
        append("select one from table $tableNameExpr")
        append(" and store the result in $resultVar")

        if (waitFlag) append(" and wait")
        if (where != null) append(" where...")
    }
}
