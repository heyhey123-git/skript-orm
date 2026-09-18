package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Insert One Entity")
@Description("Inserts one row. The values may be written in the section body, or taken from a list variable shaped like a select result. With and wait, failures are available as the last database error; otherwise execution continues immediately and asynchronous failures are only logged. The store affected rows clause keeps the number of rows the statement affected.")
@Example(
    """insert one entity into table "users" and wait:
    values:
        name: "Alice"
        age: 25
if last database error is set:
    send "Insert failed: %last database error%"
"""
)
@Example(
    """select one entity from table "users" and store the result in {_user::*}:
    where all:
        name = "Alice"
insert one {_user::*} into table "archived_users"
"""
)
@Since("1.0.0")
class SecInsertOne : SecWriteBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecInsertOne::class.java,
                "insert one [entity] into [table] %string% [and store affected rows in %-number%] [wait:and wait]",
                "insert one [entity] %objects% into [table] %string% [and store affected rows in %-number%] [wait:and wait]"
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
    ): WriteResult {
        // Only the supplied columns are part of the statement. A column the values omit is not
        // inserted at all, so the database default applies instead of a NULL written by the ORM.
        return queries.insertOne(requireNotNull(singleValues)).execute(table)
    }

    override fun toString(event: Event?, debug: Boolean) = "insert one into table $tableNameExpr"
}
