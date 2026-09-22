package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.config.SectionNode
import ch.njol.skript.doc.*
import ch.njol.skript.effects.Delay
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Section
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.TriggerItem
import ch.njol.util.Kleenean
import io.github.heyhey123.skriptorm.SkriptOrm
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.skript.utils.ConnectionScope
import io.github.heyhey123.skriptorm.skript.utils.DatabaseWork
import io.github.heyhey123.skriptorm.skript.utils.SkriptDatabaseErrors
import io.github.heyhey123.skriptorm.skript.utils.SkriptLocalVariables
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.DataType
import io.github.heyhey123.skriptorm.type.NbtDataType
import io.github.heyhey123.skriptorm.type.nbt.NbtSupport
import io.github.heyhey123.skriptorm.utils.SyncDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Register Database Table")
@Description("Registers a table schema in the current database and waits for registration. Types come from the connected database. At least one column and at most one primary key are allowed; auto increment requires primary key. Failures are exposed as the last database error.")
@Example(
    """register a database table "users":
    id: bigint, primary key, auto increment, not null
    name: string(64), not null
    age: int, nullable
"""
)
@Since("1.0.0")
class SecRegisterTable : Section() {

    companion object {

        private val COLUMN_PATTERN = Regex("^\\s*([\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{M}\\p{Nd}_]*)\\s*:\\s*([a-zA-Z][a-zA-Z0-9]*)(?:\\s*\\(\\s*(\\d+)\\s*\\))?(.*)$")

        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
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
        if (this.trigger == null) return walk(event, false)
        DatabaseWork.clearErrorForStatement(actualEvent)

        val database = ConnectionScope.resolve(actualEvent)
            ?: return fail(actualEvent, ConnectionScope.noConnectionMessage())
        // MySQL and its relatives commit the open transaction when they see DDL, so a table registered
        // inside one would silently end it. Refusing keeps "all of it or none of it" true.
        if (ConnectionScope.transaction(actualEvent) != null) {
            return fail(
                actualEvent,
                "A table cannot be registered inside a database transaction, because creating it would commit that transaction."
            )
        }
        val tableName = tableNameExpr.getSingle(actualEvent)
            ?: return fail(actualEvent, "Table name is null.")
        if (database.tables.containsKey(tableName)) {
            return fail(actualEvent, "Table '$tableName' is already registered.")
        }

        val table = try {
            Table(
                tableName,
                columns.map { raw ->
                    val type = requireNotNull(database.dataTypes[raw.typeCode]) {
                        "Data type '${raw.typeCode}' is not supported by the connected database."
                    }
                    // NBT is the one type that needs another plugin, so a missing SkBee is refused
                    // here, while the table is being declared, rather than left to fail on the first
                    // row that is read or written.
                    require(!(type is NbtDataType && !NbtSupport.isAvailable)) {
                        "Data type 'nbtcompound' cannot be used: ${NbtSupport.unavailableReason}."
                    }
                    createColumn(raw, type)
                }
            )
        } catch (error: IllegalArgumentException) {
            return fail(actualEvent, error.message ?: "Invalid table definition.")
        }

        if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) {
            return fail(actualEvent, "Database lifecycle is shutting down.")
        }

        val continuation = next
        val localVariables = SkriptLocalVariables.remove(actualEvent)
        Delay.addDelayedEvent(actualEvent)
        SkriptOrm.ioScope.launch {
            var failure: Throwable? = null
            try {
                database.registerTable(table)
            } catch (_: CancellationException) {
                return@launch
            } catch (error: Throwable) {
                failure = error
            }

            withContext(NonCancellable + SyncDispatcher) {
                if (!SkriptOrm.instance.isEnabled || Database.isShuttingDown) return@withContext
                try {
                    if (localVariables != null) SkriptLocalVariables.restore(actualEvent, localVariables)
                    if (failure != null) {
                        SkriptDatabaseErrors.set(actualEvent, failure)
                        this@SecRegisterTable.error(SkriptDatabaseErrors.messageOf(failure))
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

    private fun fail(event: Event, message: String): TriggerItem? {
        DatabaseWork.report(event, this, message)
        return walk(event, false)
    }

    override fun toString(event: Event?, debug: Boolean): String = "register database table $tableNameExpr"
}
