package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.queries.Update
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import javax.sql.DataSource

open class JdbcUpdate(
    values: Map<String, Any?>,
    limit: Int?,
    where: WhereClause?,
    override val dataSource: DataSource,
    private val dialect: JdbcDialect
) : Update(values, limit, where), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        require(values.isNotEmpty()) { "Update values cannot be empty." }
        val columns = values.keys.toList()
        val baseSql = buildString {
            append("UPDATE ${table.name} SET ${columns.joinToString(", ") { "$it = ?" }}")
            where?.let { append(" ${JdbcConditionTranslator.translate(it)}") }
        }
        val sql = limit?.let {
            require(it > 0) { "Update limit must be positive." }
            dialect.applyUpdateLimit(baseSql, it)
        } ?: baseSql
        return executeUpdate(sql) { statement ->
            var index = 1
            columns.forEach { key ->
                val column = requireNotNull(table.getColumnByName(key)) { "Table ${table.name} does not have column $key." }
                statement.bindValue(index++, values[key], column.type)
            }
            where?.conditions?.forEach { condition ->
                val column = requireNotNull(table.getColumnByName(condition.left)) { "Table ${table.name} does not have column ${condition.left}." }
                index = JdbcConditionTranslator.fillConditionParameters(condition, statement, index, column.type)
            }
        }
    }
}
