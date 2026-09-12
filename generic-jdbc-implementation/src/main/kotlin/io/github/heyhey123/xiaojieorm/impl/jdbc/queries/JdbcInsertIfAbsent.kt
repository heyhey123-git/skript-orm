package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.queries.InsertIfAbsent
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Connection

open class JdbcInsertIfAbsent(
    values: Map<String, Any?>, override val connection: Connection
) : InsertIfAbsent(values), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        val tableName = table.name
        val columns = values.keys.joinToString(", ")
        val placeholders = values.keys.joinToString(", ") { "?" }
        val sql = "INSERT IGNORE INTO $tableName ($columns) VALUES ($placeholders)"
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
