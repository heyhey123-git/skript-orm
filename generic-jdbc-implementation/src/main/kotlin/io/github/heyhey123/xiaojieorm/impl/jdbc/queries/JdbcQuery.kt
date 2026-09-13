package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.impl.jdbc.result.JdbcDataCursor
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.PreparedStatement
import javax.sql.DataSource

/**
 * Shared JDBC query execution boundary.
 * Connections are acquired only when execute is called.
 */
interface JdbcQuery {
    val dataSource: DataSource

    fun bindWhere(table: Table, where: WhereClause?, statement: PreparedStatement, startIndex: Int = 1): Int {
        var index = startIndex
        where?.conditions?.forEach { condition ->
            val column = requireNotNull(table.getColumnByName(condition.left)) {
                "Table ${table.name} does not have column ${condition.left}."
            }
            index = JdbcConditionTranslator.fillConditionParameters(condition, statement, index, column.type)
        }
        return index
    }

    fun executeUpdate(sql: String, bind: (PreparedStatement) -> Unit): WriteResult =
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                bind(statement)
                WriteResult(statement.executeUpdate())
            }
        }

    fun executeCursor(sql: String, bind: (PreparedStatement) -> Unit): CursorResult {
        val connection = dataSource.connection
        try {
            val statement = connection.prepareStatement(sql)
            try {
                bind(statement)
                val resultSet = statement.executeQuery()
                return CursorResult(JdbcDataCursor(resultSet, statement, connection))
            } catch (error: Throwable) {
                statement.close()
                throw error
            }
        } catch (error: Throwable) {
            connection.close()
            throw error
        }
    }
}
