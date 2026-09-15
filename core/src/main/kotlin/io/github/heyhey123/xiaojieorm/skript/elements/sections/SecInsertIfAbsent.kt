package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

@Name("Insert Entity If Absent")
@Description("Inserts one row only when the database implementation considers it absent. Support and conflict rules depend on the implementation. With and wait, failures are available as the last database error; otherwise asynchronous failures are only logged.")
@Example("""
insert entity if absent into table "users" and wait:
    values:
        id: {_id}
        name: "Alice"
""")
@Since("1.0")
class SecInsertIfAbsent : SecWriteBase() {
    companion object {
        init {
            Skript.registerSection(SecInsertIfAbsent::class.java, "insert [one] [entity] if absent into [table] %string% [wait:and wait]")
        }
    }

    override val tableNameIndex = 0

    override suspend fun executeWrite(
        queries: Queries,
        table: Table,
        singleValues: Map<String, Any?>?,
        multipleValues: List<Map<String, Any?>>?,
        whereClause: WhereClause?,
        extraArguments: Any?
    ) {
        queries.insertIfAbsent(requireNotNull(singleValues)).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "insert if absent into table $tableNameExpr"
}
