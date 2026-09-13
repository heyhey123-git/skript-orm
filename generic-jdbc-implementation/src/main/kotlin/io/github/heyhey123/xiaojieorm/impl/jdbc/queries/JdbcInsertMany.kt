package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.xiaojieorm.queries.InsertMany
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Statement
import javax.sql.DataSource

open class JdbcInsertMany(valuesList: List<Map<String, Any?>>, override val dataSource: DataSource) : InsertMany(valuesList), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        if (valuesList.isEmpty()) return WriteResult(0)
        val columns = valuesList.first().keys.toList()
        require(columns.isNotEmpty()) { "Insert values cannot be empty." }
        val expectedColumns = columns.toSet()
        valuesList.forEachIndexed { rowIndex, row ->
            require(row.keys == expectedColumns) { "Batch row ${rowIndex + 1} does not contain the same columns as the first row." }
        }
        val sql = "INSERT INTO ${table.name} (${columns.joinToString(", ")}) VALUES (${columns.joinToString(", ") { "?" }})"
        return dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                valuesList.forEach { row ->
                    columns.forEachIndexed { index, key ->
                        val column = requireNotNull(table.getColumnByName(key)) { "Table ${table.name} does not have column $key." }
                        statement.setObject(index + 1, row[key], (column.type as JdbcDataType).jdbcType)
                    }
                    statement.addBatch()
                }
                val counts = statement.executeBatch()
                var affected = 0
                counts.forEach { count ->
                    when {
                        count >= 0 -> affected += count
                        count == Statement.SUCCESS_NO_INFO -> affected += 1
                        count == Statement.EXECUTE_FAILED -> error("A JDBC batch insert operation failed.")
                    }
                }
                WriteResult(affected)
            }
        }
    }
}
