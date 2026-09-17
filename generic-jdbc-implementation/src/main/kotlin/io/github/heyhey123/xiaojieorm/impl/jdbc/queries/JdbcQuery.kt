package io.github.heyhey123.xiaojieorm.impl.jdbc.queries

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.impl.jdbc.condition.JdbcConditionTranslator
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.result.JdbcDataCursor
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import java.sql.PreparedStatement

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
     * Statement timeout in seconds for the statements this query builds. Zero leaves the driver's
     * default unchanged; negative values are rejected when a statement is configured.
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

    /**
     * The timeout actually applied: the shorter of what the query asks for and what the connection
     * source imposes.
     *
     * A transaction uses the second to bound statements it cannot otherwise interrupt, because rolling
     * a transaction back waits for the statement running on its connection.
     */
    fun effectiveTimeoutSeconds(): Int {
        val own = queryTimeoutSeconds
        val imposed = connectionSource.statementTimeoutSeconds
        require(own >= 0 && imposed >= 0) { "JDBC query timeout must not be negative." }
        return when {
            own == 0 -> imposed
            imposed == 0 -> own
            else -> minOf(own, imposed)
        }
    }

    fun configureStatement(statement: PreparedStatement) {
        val seconds = effectiveTimeoutSeconds()
        if (seconds > 0) statement.queryTimeout = seconds
    }

    suspend fun executeUpdate(sql: String, bind: (PreparedStatement) -> Unit): WriteResult {
        val connection = connectionSource.borrow()
        try {
            return connection.prepareStatement(sql).use { statement ->
                statement.withBoundResources {
                    configureStatement(statement)
                    bind(statement)
                    WriteResult(statement.executeLargeUpdate())
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
                configureStatement(statement)
                bind(statement)
                val resultSet = statement.executeQuery()
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
}
