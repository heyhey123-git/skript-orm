package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.SelectById
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table
import javax.sql.DataSource

open class JdbcSelectById(
    id: Any,
    override val dataSource: DataSource,
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
