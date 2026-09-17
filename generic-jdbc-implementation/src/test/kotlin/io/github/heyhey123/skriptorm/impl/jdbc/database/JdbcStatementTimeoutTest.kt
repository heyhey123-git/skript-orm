package io.github.heyhey123.skriptorm.impl.jdbc.database

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Covers how the statement timeout is read from the properties a script wrote.
 *
 * The value decides whether one stuck statement can hold a pooled connection until the server
 * restarts, so the interesting cases are the ones where a script says something it did not mean: a
 * missing property, an explicit zero, and a value that is not a number at all.
 */
class JdbcStatementTimeoutTest {

    @Test
    fun `a connection without the property gets the default`() {
        assertEquals(
            JdbcDatabase.DEFAULT_STATEMENT_TIMEOUT_SECONDS,
            JdbcDatabase.statementTimeoutSeconds(mapOf("driver" to "com.mysql.cj.jdbc.Driver"))
        )
    }

    @Test
    fun `the property is read as seconds`() {
        assertEquals(
            5,
            JdbcDatabase.statementTimeoutSeconds(mapOf(JdbcDatabase.STATEMENT_TIMEOUT_PROPERTY to "5"))
        )
    }

    @Test
    fun `zero means no timeout rather than a missing value`() {
        assertEquals(
            0,
            JdbcDatabase.statementTimeoutSeconds(mapOf(JdbcDatabase.STATEMENT_TIMEOUT_PROPERTY to " 0 "))
        )
    }

    @Test
    fun `a value that is not a number is rejected instead of being ignored`() {
        val thrown = assertFailsWith<IllegalArgumentException> {
            JdbcDatabase.statementTimeoutSeconds(mapOf(JdbcDatabase.STATEMENT_TIMEOUT_PROPERTY to "soon"))
        }

        assertEquals(
            "Connection property 'statement timeout' must be a whole number of seconds, but was 'soon'.",
            thrown.message
        )
    }

    @Test
    fun `a negative value is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            JdbcDatabase.statementTimeoutSeconds(mapOf(JdbcDatabase.STATEMENT_TIMEOUT_PROPERTY to "-1"))
        }
    }
}
