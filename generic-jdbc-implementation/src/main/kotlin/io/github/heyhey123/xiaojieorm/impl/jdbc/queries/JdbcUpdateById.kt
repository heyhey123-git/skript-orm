package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.queries.UpdateById
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Connection

open class JdbcUpdateById(
    id: Any,
    values: Map<String, Any?>,
    override val connection: Connection
) : UpdateById(id, values), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        val tableName = table.name
        val setClauses = values.keys.joinToString(", ") { "$it = ?" }
        val primaryKeyColumn = table.primaryKey
            ?: throw IllegalStateException("Table ${table.name} does not have a primary key.")
        val sql = "UPDATE $tableName SET $setClauses WHERE ${primaryKeyColumn.name} = ?"
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
        preparedStatement.setObject(
            index,
            id,
            (primaryKeyColumn.type as JdbcDataType).jdbcType
        )
        val affectedRows = preparedStatement.executeUpdate()
        return WriteResult(affectedRows)
    }
}
