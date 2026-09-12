package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.result.JdbcDataCursor
import io.github.heyhey123.xiaojieorm.queries.SelectById
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Connection

open class JdbcSelectById(
    id: Any,
    val connection: Connection
) : SelectById(id) {

    override suspend fun execute(table: Table): CursorResult {
        val tableName = table.name
        val idColumn = table.primaryKey!!
        val sql = "SELECT * FROM $tableName WHERE ${idColumn.name} = ? LIMIT 1"
        val preparedStatement = connection.prepareStatement(sql)
        preparedStatement.setObject(1, id)
        val resultSet = preparedStatement.executeQuery()

        return CursorResult(JdbcDataCursor(resultSet))
    }

}
