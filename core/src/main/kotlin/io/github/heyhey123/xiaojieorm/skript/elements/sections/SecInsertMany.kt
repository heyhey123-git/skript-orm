package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

@Name("Insert Many Entities")
@Description("Inserts multiple rows. Under values, each nested block is one row. With and wait, failures are available as the last database error; otherwise execution continues immediately and asynchronous failures are only logged.")
@Example("""
insert many entities into table "users" and wait:
    values:
        first:
            name: "Alice"
            age: 25
        second:
            name: "Bob"
            age: 30
""")
@Since("1.0")
class SecInsertMany : SecWriteBase() {
    companion object {
        init {
            Skript.registerSection(SecInsertMany::class.java, "insert many [entities] into [table] %string% [wait:and wait]")
        }
    }

    override val tableNameIndex = 0
    override val supportsMultipleRows = true

    override suspend fun executeWrite(
        queries: Queries,
        table: Table,
        singleValues: Map<String, Any?>?,
        multipleValues: List<Map<String, Any?>>?,
        whereClause: WhereClause?,
        extraArguments: Any?
    ) {
        queries.insertMany(multipleValues ?: listOf(requireNotNull(singleValues))).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "insert many into table $tableNameExpr"
}
