package io.github.heyhey123.xiaojieorm.type

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DataTypeTest {
    @Test
    fun `primitive data types accept boxed JVM values`() {
        val cases = listOf(
            BooleanDataType().domainType to true,
            TinyIntDataType().domainType to 1.toByte(),
            IntDataType().domainType to 1,
            BigIntDataType().domainType to 1L,
            FloatDataType().domainType to 1.0f,
            DoubleDataType().domainType to 1.0,
        )

        cases.forEach { (domainType, value) ->
            assertTrue(
                domainType.isInstance(value),
                "${domainType.name} should accept boxed value ${value::class.java.name}",
            )
            assertTrue(!domainType.isPrimitive, "${domainType.name} must be a boxed JVM class")
        }
    }

    @Test
    fun `default converter preserves values and runtime types`() {
        val dataType = IntDataType()
        @Suppress("UNCHECKED_CAST")
        val converter = dataType.converter as ValueConverter<Int, Int>
        val value = 42

        assertSame(dataType.domainType, converter.domainType)
        assertSame(dataType.domainType, converter.storageType)
        assertEquals(value, converter.toStorage(value))
        assertEquals(value, converter.fromStorage(value))
    }
}
