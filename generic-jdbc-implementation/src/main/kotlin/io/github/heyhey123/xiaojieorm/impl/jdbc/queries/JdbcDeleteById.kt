package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.DeleteById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import javax.sql.DataSource

open class JdbcDeleteById(
    id: Any,
    override val dataSource: DataSource,
    override val dialect: JdbcDialect
) : DeleteById(id), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        val primaryKey = requireNotNull(table.primaryKey) { "Table ${table.name} does not have a primary key." }
        val whereSql = "WHERE ${dialect.quoteIdentifier(primaryKey.name)} = ?"
        return executeUpdate(dialect.delete(table.name, whereSql, null)) { statement ->
            statement.bindValue(1, id, primaryKey.type)
        }
    }
}
