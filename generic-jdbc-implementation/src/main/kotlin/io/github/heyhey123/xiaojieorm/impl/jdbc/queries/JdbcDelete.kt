package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Delete
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Connection

open class JdbcDelete(
    limit: Int?,
    where: WhereClause?,
    override val connection: Connection
) : Delete(limit, where), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        val tableName = table.name
        val sql = buildString {
            append("DELETE FROM $tableName ")
            if (where != null) {
                val whereClause = JdbcConditionTranslator.translate(where!!)
                append(whereClause)
            }
            if (limit != null) {
                append(" LIMIT $limit")
            }
        }
        val preparedStatement = connection.prepareStatement(sql)
        var index = 1
        where?.conditions?.forEach { condition ->
            val column = table.getColumnByName(condition.left)
            require(column != null) {
                "Table ${table.name} does not have column ${condition.left}."
            }
            val targetType = column.type
            index = JdbcConditionTranslator.fillConditionParameters(condition, preparedStatement, index, targetType)
        }
        val affectedRows = preparedStatement.executeUpdate()
        return WriteResult(affectedRows)
    }
}
