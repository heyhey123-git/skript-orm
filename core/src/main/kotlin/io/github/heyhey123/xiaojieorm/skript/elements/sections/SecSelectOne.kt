package io.github.heyhey123.xiaojieorm.skript.elements.sections

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Example
import ch.njol.skript.doc.Name
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.table.Table
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

class SecSelectOne : SecSelectBase() {

    companion object {
        init {
            Skript.registerSection(
                SecSelectOne::class.java,
                "select one [entity] from [table] %string% [and] store [the] [result] in %objects% [wait:and wait] [where:where (any:[neg:no] any|all:[neg:not] all)]"
            )
        }
    }

    override val tableNameIndex: Int = 0

    override val resultVarIndex: Int = 1

    override suspend fun executeQuery(
        database: Database,
        table: Table,
        whereClause: WhereClause?,
        extraArguments: Any?
    ): Map<String, Any?> {
        val result = linkedMapOf<String, Any?>()
        database.queries!!.selectOne(whereClause).execute(table).cursor.use { cursor ->
            if (cursor.next()) {
                table.columns.values.forEachIndexed { index, column ->
                    result[(index + 1).toString()] = cursor.get(column.name, column.type)
                }
            }
        }
        return result
    }

    override fun toString(event: Event?, debug: Boolean): String = buildString {
        append("select one from table $tableNameExpr")
        append(" and store the result in $resultVar")
        if (waitFlag) append(" and wait")
        if (where != null) append(" where...")
    }
}
