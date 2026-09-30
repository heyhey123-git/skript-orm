package io.github.heyhey123.skriptorm.impl.jdbc.integration

import java.sql.SQLTimeoutException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Covers the assumption the statement timeout is built on: that the driver really does stop a statement
 * which outlives it.
 *
 * `Statement.setQueryTimeout` is a hint rather than a guarantee, and each driver keeps it its own way:
 * Connector/J asks the server to kill the query from a second connection, while MariaDB Connector/J
 * sends the statement with a `max_statement_time` prefix and lets the server stop it. Both are covered
 * because the addon's promise is the same for either, and neither follows from the other.
 *
 * The addon's own wiring is covered by unit tests; this one is about the driver underneath it, which is
 * why it talks to the connection directly rather than through a query object.
 */
abstract class StatementTimeoutIntegrationTest : MysqlIntegrationTestBase() {

    @Test
    fun `the driver cancels a statement that outlives its timeout`() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.queryTimeout = 1

                val started = System.nanoTime()
                assertFailsWith<SQLTimeoutException> {
                    statement.executeQuery("SELECT SLEEP(3)")
                }
                val elapsedMillis = (System.nanoTime() - started) / 1_000_000
                assertTrue(elapsedMillis < 2_500, "the cancelled statement ran for ${elapsedMillis}ms")
            }

            // The cancellation leaves the connection usable, which is what the message the addon raises
            // promises a script. If this ever stops being true, that promise has to change with it.
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT 1").use { rows ->
                    assertTrue(rows.next(), "the connection did not survive the cancellation")
                }
            }
        }
    }
}
