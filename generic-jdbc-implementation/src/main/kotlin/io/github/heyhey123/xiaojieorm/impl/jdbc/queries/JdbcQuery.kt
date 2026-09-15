package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.result.JdbcDataCursor
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.PreparedStatement
import javax.sql.DataSource

/**
 * Shared blocking JDBC execution boundary; `suspend` methods do not switch dispatchers.
 *
 * Each execution borrows a connection. Update executions close all JDBC resources before returning.
 * Cursor executions transfer the result set, statement, connection, and bound-resource cleanup to
 * the returned [JdbcDataCursor], which the caller must close.
 */
interface JdbcQuery {

    val dataSource: DataSource
    val dialect: JdbcDialect

    /**
     * Statement timeout in seconds. Zero leaves the driver's default unchanged; negative values
     * are rejected when a statement is configured.
     */
    val queryTimeoutSeconds: Int
        get() = 0

    /**
     * Binds [where] from the 1-based [startIndex] and returns the next free parameter index.
     * Referenced columns must exist; null equality conditions consume no placeholder.
     */
    fun bindWhere(table: Table, where: WhereClause?, statement: PreparedStatement, startIndex: Int = 1): Int {
        var index = startIndex
        where?.conditions?.forEach { condition ->
            val column = requireNotNull(table.getColumnByName(condition.left)) {
                "Table ${table.name} does not have column ${condition.left}."
            }
            index = JdbcConditionTranslator.fillConditionParameters(condition, statement, index, column.type)
        }
        return index
    }

    fun configureStatement(statement: PreparedStatement) {
        require(queryTimeoutSeconds >= 0) { "JDBC query timeout must not be negative." }
        if (queryTimeoutSeconds > 0) statement.queryTimeout = queryTimeoutSeconds
    }

    suspend fun executeUpdate(sql: String, bind: (PreparedStatement) -> Unit): WriteResult =
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.withBoundResources {
                    configureStatement(statement)
                    bind(statement)
                    WriteResult(statement.executeLargeUpdate())
                }
            }
        }

    suspend fun executeCursor(sql: String, bind: (PreparedStatement) -> Unit): CursorResult {
        val connection = dataSource.connection
        try {
            val statement = connection.prepareStatement(sql)
            try {
                configureStatement(statement)
                bind(statement)
                val resultSet = statement.executeQuery()
                return CursorResult(
                    JdbcDataCursor(resultSet, statement, connection) {
                        statement.releaseBoundResources()
                    }
                )
            } catch (error: Throwable) {
                try {
                    statement.releaseBoundResources()
                } catch (cleanupError: Throwable) {
                    error.addSuppressed(cleanupError)
                }
                try {
                    statement.close()
                } catch (cleanupError: Throwable) {
                    error.addSuppressed(cleanupError)
                }
                throw error
            }
        } catch (error: Throwable) {
            try {
                connection.close()
            } catch (cleanupError: Throwable) {
                error.addSuppressed(cleanupError)
            }
            throw error
        }
    }
}
