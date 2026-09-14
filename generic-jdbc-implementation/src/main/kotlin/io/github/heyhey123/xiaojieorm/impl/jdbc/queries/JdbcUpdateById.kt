package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.UpdateById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import javax.sql.DataSource

open class JdbcUpdateById(
    id: Any,
    values: Map<String, Any?>,
    override val dataSource: DataSource,
    override val dialect: JdbcDialect
) : UpdateById(id, values), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Update values cannot be empty." }
        val primaryKey = requireNotNull(table.primaryKey) { "Table ${table.name} does not have a primary key." }
        require(primaryKey.name !in values) { "The primary key must not be included in update values." }
        val columns = values.keys.toList()
        val whereSql = "WHERE ${dialect.quoteIdentifier(primaryKey.name)} = ?"
        val sql = dialect.update(table.name, columns, whereSql, null)
        return executeUpdate(sql) { statement ->
            columns.forEachIndexed { index, key ->
                val column = requireNotNull(table.getColumnByName(key)) { "Table ${table.name} does not have column $key." }
                statement.bindValue(index + 1, values[key], column.type)
            }
            statement.bindValue(columns.size + 1, id, primaryKey.type)
        }
    }
}
