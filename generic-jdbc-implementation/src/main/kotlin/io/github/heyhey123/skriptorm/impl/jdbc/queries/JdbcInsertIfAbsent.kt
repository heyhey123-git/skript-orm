package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.queries.InsertIfAbsent
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table

open class JdbcInsertIfAbsent(
    values: Map<String, Any?>,
    override val connectionSource: JdbcConnectionSource,
    override val dialect: JdbcDialect
) : InsertIfAbsent(values), JdbcQuery {

    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Insert values cannot be empty." }
        val columns = values.keys.toList()
        val sql = dialect.insertIfAbsent(table.name, columns)
        return executeUpdate(sql) { statement ->
            columns.forEachIndexed { index, key ->
                val column = requireNotNull(table.getColumnByName(key)) { "Table ${table.name} does not have column $key." }
                statement.bindValue(index + 1, values[key], column.type)
            }
        }
    }
}
