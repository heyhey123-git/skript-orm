package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.SelectMany
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table
import javax.sql.DataSource

open class JdbcSelectMany(
    where: WhereClause?,
    override val dataSource: DataSource,
    override val dialect: JdbcDialect
) : SelectMany(where), JdbcQuery {
    override suspend fun execute(table: Table): CursorResult {
        val whereSql = where?.let { JdbcConditionTranslator.translate(it, dialect) }
        return executeCursor(dialect.select(table.name, whereSql)) { statement ->
            bindWhere(table, where, statement)
        }
    }
}
