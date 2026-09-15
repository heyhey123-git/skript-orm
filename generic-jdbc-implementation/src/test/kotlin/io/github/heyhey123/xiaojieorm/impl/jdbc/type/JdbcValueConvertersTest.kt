package io.github.heyhey123.xiaojieorm.impl.jdbc.type

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JdbcValueConvertersTest {
    @Test
    fun `uuid round trips through exactly sixteen bytes`() {
        val value = UUID.fromString("00112233-4455-6677-8899-aabbccddeeff")

        val bytes = UuidJdbcConverter.toStorage(value)

        assertEquals(16, bytes.size)
        assertContentEquals(
            byteArrayOf(0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, -120, -103, -86, -69, -52, -35, -18, -1),
            bytes,
        )
        assertEquals(value, UuidJdbcConverter.fromStorage(bytes))
    }

    @Test
    fun `uuid rejects malformed storage length`() {
        assertFailsWith<IllegalArgumentException> { UuidJdbcConverter.fromStorage(ByteArray(15)) }
        assertFailsWith<IllegalArgumentException> { UuidJdbcConverter.fromStorage(ByteArray(17)) }
    }
}
