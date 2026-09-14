package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.InsertMany
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Statement
import javax.sql.DataSource

open class JdbcInsertMany(
    valuesList: List<Map<String, Any?>>,
    override val dataSource: DataSource,
    override val dialect: JdbcDialect
) : InsertMany(valuesList), JdbcQuery {
    override suspend fun execute(table: Table): WriteResult {
        if (valuesList.isEmpty()) return WriteResult(0)
        val columns = valuesList.first().keys.toList()
        require(columns.isNotEmpty()) { "Insert values cannot be empty." }
        val expectedColumns = columns.toSet()
        valuesList.forEachIndexed { rowIndex, row ->
            require(row.keys == expectedColumns) {
                "Batch row ${rowIndex + 1} does not contain the same columns as the first row."
            }
        }

        val sql = dialect.insert(table.name, columns)
        return dataSource.connection.use { connection ->
            val previousAutoCommit = connection.autoCommit
            connection.autoCommit = false
            try {
                val result = connection.prepareStatement(sql).use { statement ->
                    try {
                        valuesList.forEach { row ->
                            columns.forEachIndexed { index, key ->
                                val column = requireNotNull(table.getColumnByName(key)) {
                                    "Table ${table.name} does not have column $key."
                                }
                                statement.bindValue(index + 1, row[key], column.type)
                            }
                            statement.addBatch()
                        }

                        val counts = statement.executeLargeBatch()
                        var affected = 0L
                        counts.forEach { count ->
                            when {
                                count >= 0L -> affected = Math.addExact(affected, count)
                                count == Statement.SUCCESS_NO_INFO.toLong() ->
                                    affected = Math.addExact(affected, 1L)
                                count == Statement.EXECUTE_FAILED.toLong() ->
                                    error("A JDBC batch insert operation failed.")
                                else -> error("The JDBC driver returned an invalid batch update count: $count.")
                            }
                        }
                        WriteResult(affected)
                    } finally {
                        statement.releaseBoundResources()
                    }
                }
                connection.commit()
                result
            } catch (error: Throwable) {
                try {
                    connection.rollback()
                } catch (rollbackError: Throwable) {
                    error.addSuppressed(rollbackError)
                }
                throw error
            } finally {
                try {
                    connection.autoCommit = previousAutoCommit
                } catch (_: Throwable) {
                    // The connection is about to be closed; preserve the primary operation result/failure.
                }
            }
        }
    }
}
