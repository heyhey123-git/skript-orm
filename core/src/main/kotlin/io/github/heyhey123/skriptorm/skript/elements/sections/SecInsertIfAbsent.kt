package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Insert Entity If Absent")
@Description("Inserts one row only when the database implementation considers it absent. The values may be written in the section body, or taken from a list variable shaped like a select result. Support and conflict rules depend on the implementation. With and wait, failures are available as the last database error; otherwise asynchronous failures are only logged.")
@Example(
    """
insert entity if absent into table "users" and wait:
    values:
        id: {_id}
        name: "Alice"
"""
)
@Example(
    """
insert entity {_user::*} if absent into table "archived_users"
"""
)
@Since("1.0")
class SecInsertIfAbsent : SecWriteBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecInsertIfAbsent::class.java,
                "insert [one] [entity] if absent into [table] %string% [wait:and wait]",
                "insert [one] [entity] %objects% if absent into [table] %string% [wait:and wait]"
            )
        }
    }

    override fun valuesExpressionIndex(matchedPattern: Int) = if (matchedPattern == 0) -1 else 0

    override suspend fun executeWrite(
        queries: Queries,
        table: Table,
        singleValues: Map<String, Any?>?,
        multipleValues: List<Map<String, Any?>>?,
        whereClause: WhereClause?,
        extraArguments: Any?
    ) {
        // As in SecInsertOne: a column the values omit is absent from the statement, so the database
        // default applies rather than a NULL written by the ORM.
        queries.insertIfAbsent(requireNotNull(singleValues)).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "insert if absent into table $tableNameExpr"
}
