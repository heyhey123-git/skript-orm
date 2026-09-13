package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.queries.DeleteById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import javax.sql.DataSource

open class JdbcDeleteById(id: Any, override val dataSource: DataSource) : DeleteById(id), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        val primaryKey = requireNotNull(table.primaryKey) { "Table ${table.name} does not have a primary key." }
        return executeUpdate("DELETE FROM ${table.name} WHERE ${primaryKey.name} = ?") { statement ->
            statement.setObject(1, id, (primaryKey.type as JdbcDataType).jdbcType)
        }
    }
}
