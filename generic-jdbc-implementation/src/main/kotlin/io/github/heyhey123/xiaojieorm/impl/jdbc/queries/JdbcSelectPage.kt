package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.queries.SelectPage
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.JDBCType
import javax.sql.DataSource

open class JdbcSelectPage(pageSize: Int, pageIndex: Int, where: WhereClause?, override val dataSource: DataSource) : SelectPage(pageSize, pageIndex, where), JdbcQuery {
    override suspend fun execute(table: Table): CursorResult {
        require(pageSize > 0) { "Page size must be positive." }
        require(pageIndex >= 1) { "Page index must be at least one." }
        val offset = (pageIndex.toLong() - 1L) * pageSize.toLong()
        val sql = buildString {
            append("SELECT * FROM ${table.name}")
            where?.let { append(" ${JdbcConditionTranslator.translate(it)}") }
            append(" LIMIT ? OFFSET ?")
        }
        return executeCursor(sql) { statement ->
            var index = bindWhere(table, where, statement)
            statement.setObject(index++, pageSize, JDBCType.INTEGER)
            statement.setObject(index, offset, JDBCType.BIGINT)
        }
    }
}
