package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.InsertOne
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import javax.sql.DataSource

open class JdbcInsertOne(
    values: Map<String, Any?>,
    override val dataSource: DataSource,
    override val dialect: JdbcDialect
) : InsertOne(values), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Insert values cannot be empty." }
        val columns = values.keys.toList()
        val sql = dialect.insert(table.name, columns)
        return executeUpdate(sql) { statement ->
            columns.forEachIndexed { index, key ->
                val column = requireNotNull(table.getColumnByName(key)) { "Table ${table.name} does not have column $key." }
                statement.bindValue(index + 1, values[key], column.type)
            }
        }
    }
}
