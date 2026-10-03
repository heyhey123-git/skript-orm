package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.impl.jdbc.database.MultiRowInsertDialect
import io.github.heyhey123.skriptorm.queries.InsertMany
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.DataType
import java.sql.Connection
import java.sql.Statement

/**
 * JDBC insert of multiple rows. MySQL uses parameterized multi-row statements; other dialects use
 * JDBC batches. Every row must have the same column set, and the first row determines binding order.
 * An empty input succeeds with an exact count of zero.
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
        val types = columns.map { key ->
            requireNotNull(table.getColumnByName(key)) { "Table ${table.name} does not have column $key." }.type
        }

        val connection = connectionSource.borrow()
        try {
            val multiRowDialect = dialect as? MultiRowInsertDialect
            if (multiRowDialect != null) {
                return executeMultiRow(table, columns, types, connection, multiRowDialect)
            }
            val sql = dialect.insert(table.name, columns)
            return connection.prepareStatement(sql).use { statement ->
                statement.withBoundResources {
                    val timeout = configureStatement(statement)
                    valuesList.forEach { row ->
                        columns.forEachIndexed { index, key ->
                            statement.bindValue(index + 1, row[key], types[index])
                        }
                        statement.addBatch()
                    }

                    val counts = executeWithTimeoutReported(timeout) { statement.executeLargeBatch() }
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

    private fun executeMultiRow(
        table: Table,
        columns: List<String>,
        types: List<DataType<*>>,
        connection: Connection,
        multiRowDialect: MultiRowInsertDialect
    ): WriteResult {
        // Direct query callers may bypass the core write splitter.
        val rowsPerStatement = maxOf(1, MAX_BOUND_VALUES / columns.size)
        var affected = 0L
        for (start in valuesList.indices step rowsPerStatement) {
            val rowCount = minOf(rowsPerStatement, valuesList.size - start)
            val sql = multiRowDialect.insertMany(table.name, columns, rowCount)
            connection.prepareStatement(sql).use { statement ->
                statement.withBoundResources {
                    val timeout = configureStatement(statement)
                    repeat(rowCount) { rowOffset ->
                        val row = valuesList[start + rowOffset]
                        columns.forEachIndexed { columnIndex, key ->
                            statement.bindValue(rowOffset * columns.size + columnIndex + 1, row[key], types[columnIndex])
                        }
                    }
                    val count = executeWithTimeoutReported(timeout) { statement.executeLargeUpdate() }
                    require(count >= 0) { "The JDBC driver returned an invalid update count: $count." }
                    affected = Math.addExact(affected, count)
                }
            }
        }
        return WriteResult(affected)
    }

    private companion object {
        const val MAX_BOUND_VALUES = 30_000
    }
}
