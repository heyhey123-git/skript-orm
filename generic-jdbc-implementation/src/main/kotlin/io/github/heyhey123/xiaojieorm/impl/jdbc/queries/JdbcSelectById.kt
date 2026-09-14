package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.queries.SelectById
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table
import javax.sql.DataSource

open class JdbcSelectById(id: Any, override val dataSource: DataSource) : SelectById(id), JdbcQuery {
    override suspend fun execute(table: Table): CursorResult {
        val primaryKey = requireNotNull(table.primaryKey) { "Table ${table.name} does not have a primary key." }
        return executeCursor("SELECT * FROM ${table.name} WHERE ${primaryKey.name} = ? LIMIT 1") { statement ->
            statement.bindValue(1, id, primaryKey.type)
        }
    }
}
