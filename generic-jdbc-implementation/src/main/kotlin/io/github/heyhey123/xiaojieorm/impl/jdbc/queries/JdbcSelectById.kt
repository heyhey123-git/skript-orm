package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.SelectById
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table

open class JdbcSelectById(
    id: Any,
    override val connectionSource: JdbcConnectionSource,
    override val dialect: JdbcDialect
) : SelectById(id), JdbcQuery {

    override suspend fun execute(table: Table): CursorResult {
        val primaryKey = requireNotNull(table.primaryKey) { "Table ${table.name} does not have a primary key." }
        val whereSql = "WHERE ${dialect.quoteIdentifier(primaryKey.name)} = ?"
        return executeCursor(dialect.selectOne(table.name, whereSql)) { statement ->
            statement.bindValue(1, id, primaryKey.type)
        }
    }
}
