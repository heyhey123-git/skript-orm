package io.github.heyhey123.skriptorm.skript.utils

import io.github.heyhey123.skriptorm.database.Database
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The wording a script reads when it names a connection that is not there, and the note an operator
 * reads in the console when that is because the connection was closed rather than never created.
 *
 * What keeps Skript's classes out of this source set is the config node types they read, and this one
 * only asks the scope for a message: no event is ever handed to it, so neither Skript nor Bukkit has
 * to be on the classpath to pin the wording down.
 */
class UnknownConnectionMessageTest {

    @Test
    fun `a name that was closed says so instead of reporting that it never existed`() {
        Database.noteClosed("closed-by-unit-test")

        val message = ConnectionScope.unknownConnectionMessage("closed-by-unit-test")

        assertTrue("has since been closed" in message, "unexpected message: $message")
        assertTrue("no connection has been created yet" !in message, "unexpected message: $message")
    }

    @Test
    fun `a name that was never connected keeps the wording scripts already read`() {
        val message = ConnectionScope.unknownConnectionMessage("never-connected-by-unit-test")

        assertTrue("no connection has been created yet" in message, "unexpected message: $message")
        assertTrue("has since been closed" !in message, "unexpected message: $message")
    }

    @Test
    fun `asking for a closed name also reaches the console, and a typo does not`() {
        Database.noteClosed("closed-by-console-test")
        val warnings = mutableListOf<String>()
        Database.warn = { message -> warnings += message }
        try {
            ConnectionScope.unknownConnectionMessage("closed-by-console-test")
            assertTrue(
                warnings.any { "closed-by-console-test" in it },
                "the operator should be able to see it, saw: $warnings"
            )

            val quiet = warnings.size
            ConnectionScope.unknownConnectionMessage("never-connected-by-console-test")
            assertEquals(quiet, warnings.size, "a name nobody created should stay out of the console")
        } finally {
            Database.warn = {}
        }
    }
}
