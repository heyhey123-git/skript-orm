package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.queries.Delete
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table

open class JdbcDelete(
    limit: Int?,
    where: WhereClause?,
    override val connectionSource: JdbcConnectionSource,
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
