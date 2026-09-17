package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.queries.InsertMany
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.Statement

/**
 * JDBC batch insert. Every row must have the same column set; the first row's key order determines
 * binding order. An empty batch succeeds with an exact count of zero. `SUCCESS_NO_INFO` makes the
 * returned [WriteResult] count inexact, while `EXECUTE_FAILED` fails the operation.
 */
open class JdbcInsertMany(
    valuesList: List<Map<String, Any?>>,
    override val connectionSource: JdbcConnectionSource,
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
        val connection = connectionSource.borrow()
        try {
            return connection.prepareStatement(sql).use { statement ->
                statement.withBoundResources {
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
                    var countExact = true
                    counts.forEach { count ->
                        when {
                            count >= 0L -> affected = Math.addExact(affected, count)

                            count == Statement.SUCCESS_NO_INFO.toLong() -> countExact = false

                            count == Statement.EXECUTE_FAILED.toLong() ->
                                error("A JDBC batch insert operation failed.")

                            else -> error("The JDBC driver returned an invalid batch update count: $count.")
                        }
                    }
                    WriteResult(affected, countExact)
                }
            }
        } finally {
            connectionSource.release(connection)
        }
    }
}
