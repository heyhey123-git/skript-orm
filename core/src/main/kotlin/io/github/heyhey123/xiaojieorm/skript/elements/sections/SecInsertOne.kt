package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.doc.*
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.table.Table
import org.bukkit.event.Event

@Name("Insert One Entity")
@Description("Inserts one row. The values may be written in the section body, or taken from a list variable shaped like a select result. With and wait, failures are available as the last database error; otherwise execution continues immediately and asynchronous failures are only logged.")
@Example(
    """
insert one entity into table "users" and wait:
    values:
        name: "Alice"
        age: 25
if last database error is set:
    send "Insert failed: %last database error%"
"""
)
@Example(
    """
select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = "Alice"
insert one {_user::*} into table "archived_users":
"""
)
@Since("1.0")
class SecInsertOne : SecWriteBase() {

    companion object {
        init {
            Skript.registerSection(
                SecInsertOne::class.java,
                "insert one [entity] into [table] %string% [wait:and wait]",
                "insert one [entity] %objects% into [table] %string% [wait:and wait]"
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
        // Only the supplied columns are part of the statement. A column the values omit is not
        // inserted at all, so the database default applies instead of a NULL written by the ORM.
        queries.insertOne(requireNotNull(singleValues)).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "insert one into table $tableNameExpr"
}
