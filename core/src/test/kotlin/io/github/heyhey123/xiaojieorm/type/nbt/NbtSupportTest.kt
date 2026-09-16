package io.github.heyhey123.xiaojieorm.type.nbt

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Pins what NBT support does when SkBee is not installed, which is what the unit test classpath is.
 *
 * The behaviour with SkBee present can only be shown on a server that has it, and the Skript server
 * test does that. What is left for here is the other half: a missing SkBee is a normal state rather
 * than a failure, so the type registry can still be built, a value that is not a compound is left
 * alone, and the operations that genuinely need SkBee say which plugin is missing.
 */
class NbtSupportTest {

    @Test
    fun `nbt is unavailable without skbee`() {
        assertFalse(NbtSupport.isAvailable)
    }

    @Test
    fun `the domain falls back to any so a registry can be built`() {
        assertSame(Any::class.java, NbtSupport.domainType)
    }

    @Test
    fun `a value that is not a compound is left alone`() {
        val value = "not a compound"

        assertFalse(NbtSupport.isNbt(value))
        assertSame(value, NbtSupport.normalize(value))
        assertFalse(NbtSupport.isNbt(null))
    }

    @Test
    fun `the operations that need skbee name the missing plugin`() {
        val operations = listOf<Pair<String, () -> Any?>>(
            "parseSnbt" to { NbtSupport.parseSnbt("{xiaojie:1}") },
            "snbt" to { NbtSupport.snbt(Any()) },
            "toBytes" to { NbtSupport.toBytes(Any()) },
            "fromBytes" to { NbtSupport.fromBytes(byteArrayOf()) }
        )

        operations.forEach { (name, operation) ->
            val failure = assertFailsWith<IllegalStateException>(name) { operation() }
            assertTrue(
                failure.message.orEmpty().contains("SkBee is not installed"),
                "$name must say which plugin is missing, but said: ${failure.message}"
            )
        }
    }
}
