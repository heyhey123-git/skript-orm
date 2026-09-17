package io.github.heyhey123.skriptorm.type.nbt

import io.github.heyhey123.skriptorm.type.nbt.standin.NBTContainer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Drives the whole NBT chain against stand-in classes shaped like SkBee's.
 *
 * What this covers that a server test cannot cover cheaply: the handles are linked and then actually
 * invoked. A handle linked with the wrong parameter types, or invoked with the wrong number of
 * arguments, fails at the call rather than at the lookup, and that is exactly the kind of mistake that
 * otherwise waits for CI and a real database to show itself.
 */
class NbtSupportStandInTest {

    private companion object {
        const val STAND_IN_ROOT = "io.github.heyhey123.skriptorm.type.nbt.standin"
    }

    @BeforeEach
    fun pointAtTheStandIn() {
        NbtSupport.useProviderForTesting(STAND_IN_ROOT)
    }

    @AfterEach
    fun pointBackAtSkbee() {
        NbtSupport.resetProviderForTesting()
    }

    @Test
    fun `the stand-in makes nbt available`() {
        assertTrue(NbtSupport.isAvailable)
        assertEquals(null, NbtSupport.unavailableReason)
        assertEquals("SkBee", NbtSupport.provider)
        // The domain is the compound interface, which is what a column holds and what a script's value
        // is an instance of; the container is the concrete class behind it.
        assertTrue(NbtSupport.domainType.isAssignableFrom(NBTContainer::class.java))
    }

    @Test
    fun `a compound is recognised and normalised into a detached one`() {
        val value = NBTContainer("{skriptorm:1}")

        assertTrue(NbtSupport.isNbt(value))
        assertIs<NBTContainer>(NbtSupport.normalize(value))
    }

    @Test
    fun `bytes written from a compound are read back as the same compound`() {
        val value = NBTContainer("{skriptorm:1}")

        val bytes = NbtSupport.toBytes(value)
        val read = NbtSupport.fromBytes(bytes)

        assertContentEquals(bytes, NbtSupport.toBytes(read))
        assertEquals("{skriptorm:1}", NbtSupport.snbt(read))
    }

    @Test
    fun `snbt parses back into a compound that renders the same text`() {
        assertEquals("{skriptorm:1}", NbtSupport.snbt(NbtSupport.parseSnbt("{skriptorm:1}")))
    }
}
