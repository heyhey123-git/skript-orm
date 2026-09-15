package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import ch.njol.skript.config.SectionNode
import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.Trigger
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import io.github.heyhey123.xiaojieorm.XiaojieOrm
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.skript.utils.ErrorPrinter
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.xiaojieorm.skript.utils.SkriptLocalVariables
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.utils.SyncDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event

@Name("Register Database Table")
@Description("Registers a table schema in the current database and waits for registration. Types come from the connected database. At least one column and at most one primary key are allowed; auto increment requires primary key. Failures are exposed as the last database error.")
@Example("""
register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
""")
@Since("1.0")
class SecRegisterTable : Section() {

    companion object {
        private val COLUMN_PATTERN = Regex("^\\s*([\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{M}\\p{Nd}_]*)\\s*:\\s*([a-zA-Z][a-zA-Z0-9]*)(?:\\s*\\(\\s*(\\d+)\\s*\\))?(.*)$")

        init {
            Skript.registerSection(
                SecRegisterTable::class.java,
                "register [a] [database] table %string%"
            )
        }
    }

    private data class RawColumn(
        val name: String,
        val typeCode: String,
        val size: Int?,
        val primaryKey: Boolean,
        val autoIncrement: Boolean,
        val nullable: Boolean
    )

    private lateinit var tableNameExpr: Expression<String>
    private lateinit var columns: List<RawColumn>

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
        columns = try {
            sectionNode.map { node ->
                require(node !is SectionNode) { "Table columns must be direct entries, not nested sections." }
                parseColumn(requireNotNull(node.key) { "Table column definition cannot be empty." })
            }
        } catch (error: IllegalArgumentException) {
            Skript.error(error.message ?: "Invalid table column definition.")
            return false
        }
        if (columns.isEmpty()) {
            Skript.error("A registered table must define at least one column.")
            return false
        }
        if (columns.map { it.name }.distinct().size != columns.size) {
            Skript.error("A registered table cannot contain duplicate column names.")
            return false
        }
        if (columns.count { it.primaryKey } > 1) {
            Skript.error("A registered table cannot contain more than one primary key.")
            return false
        }
        parser.hasDelayBefore = Kleenean.TRUE
        return true
    }

    private fun parseColumn(definition: String): RawColumn {
        val match = COLUMN_PATTERN.matchEntire(definition)
            ?: throw IllegalArgumentException(
                "Invalid column '$definition'. Expected: name: type[(size)][, primary key][, auto increment][, not null]"
            )
        val modifierTail = match.groupValues[4].trim()
        require(modifierTail.isEmpty() || modifierTail.startsWith(',')) {
            "Column modifiers in '$definition' must be separated from the type by a comma."
        }
        val modifiers = modifierTail
            .removePrefix(",")
            .split(',')
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
        val allowed = setOf("primary key", "auto increment", "not null", "nullable")
        val unknown = modifiers.firstOrNull { it !in allowed }
        require(unknown == null) { "Unknown column modifier '$unknown' in '$definition'." }
        require(modifiers.distinct().size == modifiers.size) {
            "Duplicate column modifier in '$definition'."
        }
        require("not null" !in modifiers || "nullable" !in modifiers) {
            "Column '$definition' cannot be both nullable and not null."
        }
        val primaryKey = "primary key" in modifiers
        val autoIncrement = "auto increment" in modifiers
        val size = match.groupValues[3].takeIf { it.isNotEmpty() }?.toInt()
        require(size == null || size > 0) {
            "Column '${match.groupValues[1]}' size must be greater than zero."
        }
        require(!autoIncrement || primaryKey) {
            "Auto-increment column '${match.groupValues[1]}' must also be a primary key."
        }
        return RawColumn(
            name = match.groupValues[1],
            typeCode = match.groupValues[2].lowercase(),
            size = size,
            primaryKey = primaryKey,
            autoIncrement = autoIncrement,
            nullable = "not null" !in modifiers
        )
    }

    override fun walk(event: Event?): TriggerItem? {
        val actualEvent = event ?: return walk(event, false)
        val trigger = this.trigger ?: return walk(event, false)
        SkriptDatabaseErrors.clear(actualEvent)

        val database = Database.current ?: return fail(actualEvent, trigger, "No database connected.")
        val tableName = tableNameExpr.getSingle(actualEvent)
            ?: return fail(actualEvent, trigger, "Table name is null.")
        if (database.tables.containsKey(tableName)) {
            return fail(actualEvent, trigger, "Table '$tableName' is already registered.")
        }

        val table = try {
            Table(tableName, columns.map { raw ->
                val type = requireNotNull(database.dataTypes[raw.typeCode]) {
                    "Data type '${raw.typeCode}' is not supported by the connected database."
                }
                createColumn(raw, type)
            })
        } catch (error: IllegalArgumentException) {
            return fail(actualEvent, trigger, error.message ?: "Invalid table definition.")
        }

        if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) {
            return fail(actualEvent, trigger, "Database lifecycle is shutting down.")
        }

        val continuation = next
        val localVariables = SkriptLocalVariables.remove(actualEvent)
        Delay.addDelayedEvent(actualEvent)
        XiaojieOrm.ioScope.launch {
            var failure: Throwable? = null
            try {
                database.registerTable(table)
            } catch (_: CancellationException) {
                return@launch
            } catch (error: Throwable) {
                failure = error
            }

            withContext(NonCancellable + SyncDispatcher) {
                if (!XiaojieOrm.instance.isEnabled || Database.isShuttingDown) return@withContext
                try {
                    if (localVariables != null) SkriptLocalVariables.restore(actualEvent, localVariables)
                    if (failure != null) {
                        SkriptDatabaseErrors.set(actualEvent, failure)
                        ErrorPrinter.printErrorWithDetail(trigger, failure)
                    } else {
                        SkriptDatabaseErrors.clear(actualEvent)
                    }
                    walk(continuation, actualEvent)
                } finally {
                    SkriptLocalVariables.clear(actualEvent)
                }
            }
        }
        return null
    }

    private fun <T : Any> createColumn(raw: RawColumn, type: DataType<T>): Column<T> = Column(
        name = raw.name,
        type = type,
        isPrimaryKey = raw.primaryKey,
        isAutoIncrement = raw.autoIncrement,
        isNullable = raw.nullable,
        size = raw.size
    )

    private fun fail(event: Event, trigger: Trigger, message: String): TriggerItem? {
        SkriptDatabaseErrors.set(event, message)
        ErrorPrinter.printErrorMessageWithDetail(trigger, message)
        return walk(event, false)
    }

    override fun toString(event: Event?, debug: Boolean): String = "register database table $tableNameExpr"
}
