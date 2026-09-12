package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.queries.Update
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Connection

open class JdbcUpdate(
    values: Map<String, Any?>,
    limit: Int?,
    where: WhereClause?,
    override val connection: Connection
) : Update(values, limit, where), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        val tableName = table.name
        val setClauses = values.keys.joinToString(", ") { "$it = ?" }
        val sql = buildString {
            append("UPDATE $tableName SET $setClauses ")
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
        for (key in values.keys) {
            val column = table.getColumnByName(key)
            require(column != null) {
                "Table ${table.name} does not have column $key."
            }
            preparedStatement.setObject(
                index++,
                values[key],
                (column.type as JdbcDataType).jdbcType
            )
        }
        val affectedRows = preparedStatement.executeUpdate()
        return WriteResult(affectedRows)
    }
}
