package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.result.JdbcDataCursor
import io.github.heyhey123.xiaojieorm.queries.SelectPage
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Connection
import java.sql.JDBCType

open class JdbcSelectPage(
    pageSize: Int,
    pageIndex: Int,
    where: WhereClause?,
    override val connection: Connection
) : SelectPage(pageSize, pageIndex, where), JdbcQuery {
    override suspend fun execute(table: Table): CursorResult {
        val tableName = table.name
        val offset = (pageIndex - 1) * pageSize
        val sql = buildString {
            append("SELECT * FROM $tableName ")
            if (where != null) {
                val whereClause = JdbcConditionTranslator.translate(where!!)
                append(whereClause)
            }
            append(" LIMIT ? OFFSET ?")
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

        preparedStatement.apply {
            setObject(index++, pageSize, JDBCType.INTEGER)
            setObject(index, offset, JDBCType.INTEGER)
        }
        val resultSet = preparedStatement.executeQuery()
        return CursorResult(JdbcDataCursor(resultSet))
    }
}
