package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.mockk.mockk
import io.mockk.verify
import java.sql.PreparedStatement
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/**
 * Covers which values a raw statement may bind.
 *
 * A declared statement converts a script's value into the storage form of the column it names, and can say
 * why a value does not fit that column. A raw statement names no column, so there is nothing to convert for
 * and nothing to check against: the rule is the driver's own list, and a value outside it is refused rather
 * than guessed at.
 */
class RawParameterBindingTest {

    @Test
    fun `the values a driver sends as they stand are bound in order`() {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val bytes = byteArrayOf(1, 2)

        statement.bindRawParameters(listOf("text", 1, 2L, true, bytes, 1.5, null))

        verify { statement.setObject(1, "text") }
        verify { statement.setObject(2, 1) }
        verify { statement.setObject(3, 2L) }
        verify { statement.setObject(4, true) }
        verify { statement.setObject(5, bytes) }
        verify { statement.setObject(6, 1.5) }
        verify { statement.setObject(7, null as Any?) }
    }

    /** A date is a value the driver sends as it stands, whichever of the three date classes it is. */
    @Test
    fun `a date is accepted whatever its class`() {
        val date = java.sql.Date(0)
        val timestamp = java.sql.Timestamp(0)
        val utilDate = java.util.Date(0)

        assertSame(date, requireBindable(date, 0))
        assertSame(timestamp, requireBindable(timestamp, 0))
        assertSame(utilDate, requireBindable(utilDate, 0))
    }

    /**
     * The refusal names the parameter and the class, and says what the rule is. It deliberately does not say
     * what each Skript type should be converted into: that decision belongs to the script, and a JDBC-layer
     * message listing domain types would be describing a registry it does not own.
     */
    @Test
    fun `a domain value is refused by parameter and class`() {
        val uuid = UUID.randomUUID()

        val thrown = assertFailsWith<IllegalArgumentException> { requireBindable(uuid, 1) }

        val message = thrown.message.orEmpty()
        assertEquals(true, "parameter 2" in message, message)
        assertEquals(true, "java.util.UUID" in message, message)
        assertEquals(true, "convert it in the script first" in message, message)
    }

    /** A value the driver cannot send is refused before anything is bound, so no statement is half-bound. */
    @Test
    fun `nothing is bound when a later parameter is refused`() {
        val statement = mockk<PreparedStatement>(relaxed = true)

        assertFailsWith<IllegalArgumentException> {
            statement.bindRawParameters(listOf("fine", Instant.EPOCH))
        }

        verify(exactly = 0) { statement.setObject(any<Int>(), any()) }
    }
}
