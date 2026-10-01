package io.github.heyhey123.skriptorm.skript.utils

import io.github.heyhey123.skriptorm.database.Database
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The wording a script reads when it names a connection that is not there.
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
}
