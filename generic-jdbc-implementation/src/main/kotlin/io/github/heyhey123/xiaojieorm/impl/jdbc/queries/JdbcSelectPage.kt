package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcPageParameter
import io.github.heyhey123.xiaojieorm.queries.SelectPage
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.JDBCType
import javax.sql.DataSource

open class JdbcSelectPage(
    pageSize: Int,
    pageIndex: Int,
    where: WhereClause?,
    override val dataSource: DataSource,
    override val dialect: JdbcDialect
) : SelectPage(pageSize, pageIndex, where), JdbcQuery {

    override suspend fun execute(table: Table): CursorResult {
        require(pageSize > 0) { "Page size must be positive." }
        require(pageIndex >= 1) { "Page index must be at least one." }
        val offset = Math.multiplyExact(pageIndex.toLong() - 1L, pageSize.toLong())
        val whereSql = where?.let { JdbcConditionTranslator.translate(it, dialect) }
        val primaryKey = requireNotNull(table.primaryKey) {
            "Stable pagination requires table ${table.name} to define a primary key."
        }
        val pageSql = dialect.selectPage(table.name, primaryKey.name, whereSql)
        return executeCursor(pageSql.sql) { statement ->
            var index = bindWhere(table, where, statement)
            pageSql.parameterOrder.forEach { parameter ->
                when (parameter) {
                    JdbcPageParameter.LIMIT -> statement.setObject(index++, pageSize, JDBCType.INTEGER)
                    JdbcPageParameter.OFFSET -> statement.setObject(index++, offset, JDBCType.BIGINT)
                }
            }
        }
    }
}
