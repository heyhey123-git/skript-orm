package io.github.heyhey123.xiaojieorm.impl.pg.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.queries.JdbcUpsertById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import java.sql.Connection

class PgUpsertById(
    id: Any,
    values: Map<String, Any?>,
    connection: Connection
): JdbcUpsertById(id, values, connection) {
    override suspend fun execute(table: Table): WriteResult {
        val tableName = table.name
        val columns = values.keys.joinToString(", ")
        val placeholders = values.keys.joinToString(", ") { "?" }
        val updateAssignments = values.keys.joinToString(", ") { "$it = EXCLUDED.$it" }
        val sql = """
            INSERT INTO $tableName ($columns) 
            VALUES ($placeholders) 
            ON CONFLICT (${table.primaryKey!!.name}) 
            DO UPDATE SET $updateAssignments
        """.trimIndent()
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
