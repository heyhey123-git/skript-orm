package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.queries.Delete
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import javax.sql.DataSource

open class JdbcDelete(
    limit: Int?,
    where: WhereClause?,
    override val dataSource: DataSource,
    override val dialect: JdbcDialect
) : Delete(limit, where), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        val whereSql = where?.let { JdbcConditionTranslator.translate(it, dialect) }
        val sql = dialect.delete(table.name, whereSql, limit)
        return executeUpdate(sql) { statement ->
            var index = 1
            where?.conditions?.forEach { condition ->
                val column = requireNotNull(table.getColumnByName(condition.left)) { "Table ${table.name} does not have column ${condition.left}." }
                index = JdbcConditionTranslator.fillConditionParameters(condition, statement, index, column.type)
            }
        }
    }
}
