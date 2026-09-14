package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.SelectOne
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table
import javax.sql.DataSource

open class JdbcSelectOne(
    where: WhereClause?,
    override val dataSource: DataSource,
    override val dialect: JdbcDialect
) : SelectOne(where), JdbcQuery {
    override suspend fun execute(table: Table): CursorResult {
        val whereSql = where?.let { JdbcConditionTranslator.translate(it, dialect) }
        return executeCursor(dialect.selectOne(table.name, whereSql)) { statement ->
            bindWhere(table, where, statement)
        }
    }
}
