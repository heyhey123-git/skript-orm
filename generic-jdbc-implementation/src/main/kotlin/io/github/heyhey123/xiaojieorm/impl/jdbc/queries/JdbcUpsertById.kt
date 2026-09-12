package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.queries.UpsertById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Connection

open class JdbcUpsertById(
    id: Any,
    values: Map<String, Any?>,
    override val connection: Connection
) : UpsertById(id, values), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        val tableName = table.name
        val primaryKeyColumn = table.primaryKey
            ?: throw IllegalStateException("Table ${table.name} does not have a primary key.")
        val columns = values.keys.joinToString(", ")
        val placeholders = values.keys.joinToString(", ") { "?" }
        val updateClauses = values.keys.joinToString(", ") { "$it = VALUES($it)" }
        val sql = "INSERT INTO $tableName (${primaryKeyColumn.name}, $columns) VALUES (?, $placeholders) " +
                "ON DUPLICATE KEY UPDATE $updateClauses"
        val preparedStatement = connection.prepareStatement(sql)

        // Set the primary key value
        preparedStatement.setObject(
            1,
            id,
            (primaryKeyColumn.type as JdbcDataType).jdbcType
        )

        // Set the values for the other columns
        var index = 2
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
