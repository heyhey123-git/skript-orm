package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDialect
import io.github.heyhey123.skriptorm.queries.RawUpdate
import io.github.heyhey123.skriptorm.result.WriteResult
import java.sql.PreparedStatement
import java.sql.SQLFeatureNotSupportedException

/**
 * A raw SQL statement that changes the database, reported as the count the driver returned.
 *
 * This is the statement for everything a declaration cannot express — `ALTER TABLE`, `CREATE INDEX`, a
 * statement only one server understands. The count is the driver's answer to this one statement, exactly as
 * a declared write reports it; a server returns zero for a statement it does not count, such as DDL, and
 * that zero is passed on rather than turned into something more encouraging.
 */
open class JdbcRawUpdate(
    statement: String,
    parameters: List<Any?>,
    override val connectionSource: JdbcConnectionSource,
    override val dialect: JdbcDialect
) : RawUpdate(statement, parameters), JdbcQuery {

    override suspend fun execute(): WriteResult {
        val connection = connectionSource.borrow()
        try {
            return connection.prepareStatement(statement).use { prepared ->
                prepared.withBoundResources {
                    val timeout = configureStatement(prepared)
                    prepared.bindRawParameters(parameters)
                    WriteResult(
                        executeWithTimeoutReported(timeout) { prepared.executeRaisingDatabaseErrors() }
                    )
                }
            }
        } finally {
            connectionSource.release(connection)
        }
    }

    /**
     * Runs the statement and answers how many rows it affected, or zero when the driver reports none.
     *
     * A declared write always has a count to ask for, because the dialect writes a statement the server
     * counts. A raw statement is whatever the script wrote, and DDL is where that difference shows: SQLite's
     * driver refuses `executeLargeUpdate` for a `CREATE TABLE` outright — it throws rather than answering
     * zero — while MySQL answers zero for the same statement.
     *
     * So the counted call is tried first and the count is kept whenever the driver offered one; only a
     * driver that cannot answer at all falls back to the general call, whose `false` result means the
     * statement produced no result set, which for a statement that changed something is a count of none.
     * A driver that fails for any other reason still fails here, with its own error, because the fallback is
     * taken only when the counted call is unsupported.
     */
    private fun PreparedStatement.executeRaisingDatabaseErrors(): Long {
        try {
            val counted = executeLargeUpdate()
            if (counted >= 0) return counted
        } catch (_: SQLFeatureNotSupportedException) {
            // Fall through to the call every driver has.
        }
        execute()
        return 0L
    }
}
