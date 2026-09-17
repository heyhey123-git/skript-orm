package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.impl.jdbc.result.JdbcDataCursor
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table
import java.sql.PreparedStatement
import java.sql.SQLTimeoutException

/**
 * Shared blocking JDBC execution boundary; `suspend` methods do not switch dispatchers.
 *
 * Every execution borrows a connection from its [connectionSource] and gives it back the way that
 * source decides, which is what lets the same query objects run inside a transaction without knowing
 * that they are. Update executions close all JDBC resources before returning. Cursor executions
 * transfer the result set, the statement, and the returning of the connection to the returned
 * [JdbcDataCursor], which the caller must close.
 */
interface JdbcQuery {

    val connectionSource: JdbcConnectionSource
    val dialect: JdbcDialect

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

    /**
     * Applies the statement timeout the connection source asks for, and returns it so that a statement
     * cancelled by it can say how long it was given.
     *
     * Zero leaves the driver's default alone, which is what a connection with no timeout wants. A
     * negative value is a mistake in the source rather than something a script did, so it is rejected
     * here instead of being passed to the driver.
     */
    fun configureStatement(statement: PreparedStatement): Int {
        val seconds = connectionSource.statementTimeoutSeconds()
        require(seconds >= 0) { "JDBC statement timeout must not be negative." }
        if (seconds > 0) statement.queryTimeout = seconds
        return seconds
    }

    suspend fun executeUpdate(sql: String, bind: (PreparedStatement) -> Unit): WriteResult {
        val connection = connectionSource.borrow()
        try {
            return connection.prepareStatement(sql).use { statement ->
                statement.withBoundResources {
                    val timeout = configureStatement(statement)
                    bind(statement)
                    WriteResult(
                        executeWithTimeoutReported(timeout) { statement.executeLargeUpdate() }
                    )
                }
            }
        } finally {
            connectionSource.release(connection)
        }
    }

    suspend fun executeCursor(sql: String, bind: (PreparedStatement) -> Unit): CursorResult {
        val connection = connectionSource.borrow()
        try {
            val statement = connection.prepareStatement(sql)
            try {
                val timeout = configureStatement(statement)
                bind(statement)
                val resultSet = executeWithTimeoutReported(timeout) { statement.executeQuery() }
                return CursorResult(
                    JdbcDataCursor(
                        resultSet = resultSet,
                        statement = statement,
                        releaseConnection = { connectionSource.release(connection) },
                        releaseBoundResources = { statement.releaseBoundResources() }
                    )
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
                connectionSource.release(connection)
            } catch (cleanupError: Throwable) {
                error.addSuppressed(cleanupError)
            }
            throw error
        }
    }

    /**
     * Runs [block], turning the driver's cancellation into something a script author can act on.
     *
     * What the driver throws says only that the statement was cancelled; the two things worth knowing
     * are how long it was given and that the connection is still usable afterwards, since the script's
     * next statement will be sent on it.
     */
    fun <T> executeWithTimeoutReported(seconds: Int, block: () -> T): T {
        if (seconds <= 0) return block()
        return try {
            block()
        } catch (error: SQLTimeoutException) {
            throw SQLTimeoutException(
                "The statement did not finish within $seconds second(s) and was cancelled. " +
                    "The connection is still usable; raise 'statement timeout' on it if this kind of " +
                    "statement legitimately needs longer.",
                error.sqlState,
                error.errorCode,
                error
            )
        }
    }
}
