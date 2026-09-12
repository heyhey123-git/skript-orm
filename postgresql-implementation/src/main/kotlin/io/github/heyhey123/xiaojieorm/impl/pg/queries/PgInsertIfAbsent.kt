package io.github.heyhey123.xiaojieorm.impl.pg.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.queries.JdbcInsertIfAbsent
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import java.sql.Connection

class PgInsertIfAbsent(
    values: Map<String, Any?>,
    connection: Connection
): JdbcInsertIfAbsent(values, connection) {
    override suspend fun execute(table: Table): WriteResult {
        val tableName = table.name
        val columns = values.keys.joinToString(", ")
        val placeholders = values.keys.joinToString(", ") { "?" }
        val sql = "INSERT INTO $tableName ($columns) VALUES ($placeholders) ON CONFLICT DO NOTHING"
        val preparedStatement = connection.prepareStatement(sql)
        var index = 1
        for (key in values.keys) {
            val column = table.getColumnByName(key)
            require(column != null) {
                "Table ${table.name} does not have column $key."
            }
            preparedStatement.setObject(
                index++,
                values[key],
                (column.type as JdbcDataType).jdbcType
            )
        }
        val affectedRows = preparedStatement.executeUpdate()
        return WriteResult(affectedRows)
    }
}
