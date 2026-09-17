package io.github.heyhey123.xiaojieorm.impl.jdbc.integration

import java.sql.SQLTimeoutException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Covers the assumption the statement timeout is built on: that the driver really does cancel a
 * statement that outlives it.
 *
 * `Statement.setQueryTimeout` is a hint rather than a guarantee, and Connector/J implements it by
 * asking the server to kill the query from another connection. If that did not work, a "timeout" would
 * be a description of what the addon hoped for, and a connection could stay busy forever behind it.
 *
 * The addon's own wiring is covered by unit tests; this one is about the driver underneath it, which is
 * why it talks to the connection directly rather than through a query object.
 */
class MysqlStatementTimeoutIntegrationTest : MysqlIntegrationTestBase() {

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
