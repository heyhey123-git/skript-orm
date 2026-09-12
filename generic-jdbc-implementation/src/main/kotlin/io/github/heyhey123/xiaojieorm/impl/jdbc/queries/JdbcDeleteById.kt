package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.queries.DeleteById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Connection

open class JdbcDeleteById(
    id: Any,
    override val connection: Connection
) : DeleteById(id), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        val tableName = table.name
        val primaryKeyColumn = table.primaryKey
            ?: throw IllegalStateException("Table ${table.name} does not have a primary key.")
        val sql = "DELETE FROM $tableName WHERE ${primaryKeyColumn.name} = ?"
        val preparedStatement = connection.prepareStatement(sql)
        preparedStatement.setObject(1, id)
        val affectedRows = preparedStatement.executeUpdate()
        return WriteResult(affectedRows)
    }
}
