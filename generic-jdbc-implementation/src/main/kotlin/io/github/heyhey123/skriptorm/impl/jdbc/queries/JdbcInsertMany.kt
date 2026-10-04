package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.impl.jdbc.database.MultiRowInsertDialect
import io.github.heyhey123.skriptorm.queries.InsertMany
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.DataType
import io.github.heyhey123.skriptorm.type.DefaultValueConverter
import io.github.heyhey123.skriptorm.type.PreparedDatabaseValue
import io.github.heyhey123.skriptorm.type.StorageValues
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

        val preparedRows = StorageValues.rows(table, valuesList)
        val connection = connectionSource.borrow()
        try {
            val multiRowDialect = dialect as? MultiRowInsertDialect
            if (multiRowDialect != null) {
                return executeMultiRow(table, columns, types, connection, multiRowDialect, preparedRows)
            }
            val sql = dialect.insert(table.name, columns)
            return connection.prepareStatement(sql).use { statement ->
                statement.withBoundResources {
                    val timeout = configureStatement(statement)
                    preparedRows.forEach { row ->
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
        multiRowDialect: MultiRowInsertDialect,
        preparedRows: List<Map<String, Any?>>
    ): WriteResult {
        val packetLimit = connection.prepareStatement("SELECT @@max_allowed_packet").use { statement ->
            val timeout = configureStatement(statement)
            executeWithTimeoutReported(timeout) { statement.executeQuery() }.use { result ->
                check(result.next()) { "MySQL did not return max_allowed_packet." }
                result.getLong(1).also {
                    check(it > 0L) { "MySQL returned an invalid max_allowed_packet: $it." }
                }
            }
        }
        val baseSql = multiRowDialect.insertMany(table.name, columns, 1)
        val baseBytes = baseSql.length.toLong() * 4L + 1024L
        val directlyStored = types.map { it.converter is DefaultValueConverter<*> }
        // Direct query callers may bypass the core write splitter.
        val rowsPerStatement = maxOf(1, MAX_BOUND_VALUES / columns.size)
        var affected = 0L
        var start = 0
        while (start < valuesList.size) {
            var rowCount = 0
            var estimatedBytes = baseBytes
            while (start + rowCount < valuesList.size && rowCount < rowsPerStatement) {
                val rowBytes = estimateRowBytes(valuesList[start + rowCount], columns, directlyStored)
                if (rowCount > 0 && (rowBytes == null || estimatedBytes + rowBytes > packetLimit)) break
                rowCount++
                if (rowBytes == null) break
                estimatedBytes += rowBytes
            }
            val sql = multiRowDialect.insertMany(table.name, columns, rowCount)
            connection.prepareStatement(sql).use { statement ->
                statement.withBoundResources {
                    val timeout = configureStatement(statement)
                    repeat(rowCount) { rowOffset ->
                        val row = preparedRows[start + rowOffset]
                        columns.forEachIndexed { columnIndex, key ->
                            statement.bindValue(rowOffset * columns.size + columnIndex + 1, row[key], types[columnIndex])
                        }
                    }
                    val count = executeWithTimeoutReported(timeout) { statement.executeLargeUpdate() }
                    require(count >= 0) { "The JDBC driver returned an invalid update count: $count." }
                    affected = Math.addExact(affected, count)
                }
            }
            start += rowCount
        }
        return WriteResult(affected)
    }

    private fun estimateRowBytes(
        row: Map<String, Any?>,
        columns: List<String>,
        directlyStored: List<Boolean>
    ): Long? {
        var bytes = 16L + columns.size * 16L
        columns.forEachIndexed { index, column ->
            val rawValue = row[column]
            val value = (rawValue as? PreparedDatabaseValue)?.jdbcValue() ?: rawValue
            // NULL is bound without conversion, even for a custom storage type.
            if (value != null && !directlyStored[index]) return null
            bytes += when (value) {
                null -> 16L
                is String -> value.length.toLong() * 8L + 8L
                is ByteArray -> value.size.toLong() * 2L + 16L
                is Byte, is Short, is Int, is Long, is Float, is Double, is Boolean -> 128L
                else -> return null
            }
        }
        return bytes
    }

    private companion object {
        const val MAX_BOUND_VALUES = 30_000
    }
}
