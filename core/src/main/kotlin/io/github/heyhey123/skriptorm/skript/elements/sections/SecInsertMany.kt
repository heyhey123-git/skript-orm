package io.github.heyhey123.skriptorm.skript.elements.sections

import ch.njol.skript.doc.*
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.queries.insertManyInStatements
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.skript.utils.SkriptSyntax
import io.github.heyhey123.skriptorm.table.Table
import org.bukkit.event.Event
import org.skriptlang.skript.addon.SkriptAddon

@Name("Insert Many Entities")
@Description("Inserts multiple rows. Each block under values describes one row; alternatively, supply a list variable in select-result format. Large variables are read across ticks, then written in batches. Keep the source variable unchanged until the insert finishes. The next line runs after the insert finishes. Check last database error for failures or store affected rows for the reported count.")
@Example(
    """insert many entities into table "users" and wait:
    values:
        first:
            name: "Alice"
            age: 25
        second:
            name: "Bob"
            age: 30
"""
)
@Example(
    """select many entities from table "users" and store the results in {_rows::*}:
    where all:
        active = false
insert many {_rows::*} into table "archived_users" and wait
"""
)
@Since("1.0.0")
class SecInsertMany : SecWriteBase() {

    companion object {
        fun register(addon: SkriptAddon) {
            SkriptSyntax.section(
                addon,
                SecInsertMany::class.java,
                "insert many [entities] into [table] %string% [and store affected rows in %-number%] [wait:and wait]",
                "insert many [entities] %objects% into [table] %string% [and store affected rows in %-number%] [wait:and wait]"
            )
        }
    }

    /**
     * Multi-row writes accept the `{_rows::rowIndex::columnName}` shape. The reader fills every row
     * to a common column set, because one statement can only bind one column list, so a column that
     * only some rows supply is written as NULL for the rows that omit it. A column that no row
     * supplies stays absent from the statement and falls back to the database default. This is the
     * only write that fills missing columns, and it does so because a batch cannot vary its columns.
     */
    override val supportsMultipleRows = true

    override fun valuesExpressionIndex(matchedPattern: Int) = if (matchedPattern == 0) -1 else 0

    override suspend fun executeWrite(
        queries: Queries,
        table: Table,
        singleValues: Map<String, Any?>?,
        multipleValues: List<Map<String, Any?>>?,
        whereClause: WhereClause?,
        extraArguments: Any?
    ): WriteResult =
        queries.insertManyInStatements(table, multipleValues ?: listOf(requireNotNull(singleValues)))

    override fun toString(event: Event?, debug: Boolean) = "insert many into table $tableNameExpr"
}
