package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.UpsertById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import javax.sql.DataSource

open class JdbcUpsertById(
    id: Any,
    values: Map<String, Any?>,
    override val dataSource: DataSource,
    private val dialect: JdbcDialect
) : UpsertById(id, values), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Upsert values cannot be empty." }
        val primaryKey = requireNotNull(table.primaryKey) { "Table ${table.name} does not have a primary key." }
        require(primaryKey.name !in values) { "The primary key must not be included in upsert values." }
        val columns = values.keys.toList()
        val sql = dialect.upsertById(table.name, primaryKey.name, columns)
        return executeUpdate(sql) { statement ->
            statement.bindValue(1, id, primaryKey.type)
            columns.forEachIndexed { index, key ->
                val column = requireNotNull(table.getColumnByName(key)) { "Table ${table.name} does not have column $key." }
                statement.bindValue(index + 2, values[key], column.type)
            }
        }
    }
}
