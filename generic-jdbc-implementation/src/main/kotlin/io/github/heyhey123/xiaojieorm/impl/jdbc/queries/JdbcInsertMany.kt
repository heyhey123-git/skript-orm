package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.queries.InsertMany
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Connection

open class JdbcInsertMany(
    valuesList: List<Map<String, Any?>>,
    override val connection: Connection
) : InsertMany(valuesList), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        val tableName = table.name
        if (this@JdbcInsertMany.valuesList.isEmpty()) {
            return WriteResult(0)
        }
        val columns = this@JdbcInsertMany.valuesList[0].keys.joinToString(", ")
        val placeholders = this@JdbcInsertMany.valuesList[0].keys.joinToString(", ") { "?" }
        val sql = "INSERT INTO $tableName ($columns) VALUES ($placeholders)"
        val preparedStatement = connection.prepareStatement(sql)
        for (valueMap in this@JdbcInsertMany.valuesList) {
            var index = 1
            for (key in valueMap.keys) {
                val column = table.getColumnByName(key)
                require(column != null) {
                    "Table ${table.name} does not have column $key."
                }
                preparedStatement.setObject(
                    index++,
                    valueMap[key],
                    (column.type as JdbcDataType).jdbcType
                )
            }
            preparedStatement.addBatch()
        }
        val affectedRowsArray = preparedStatement.executeBatch()
        val totalAffectedRows = affectedRowsArray.sum()
        return WriteResult(totalAffectedRows)
    }
}
