package io.github.heyhey123.xiaojieorm.type

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TypeRegistryTest {
    @Test
    fun `type id codes round trip exactly`() {
        TypeId.entries.forEach { typeId -> assertSame(typeId, TypeId.fromCode(typeId.code)) }
        assertNull(TypeId.fromCode("INT"))
        assertNull(TypeId.fromCode(""))
        assertNull(TypeId.fromCode("unknown"))
    }

    @Test
    fun `data types resolves by code and exact domain class`() {
        val int = IntDataType()
        val registry = TestDataTypes(mutableMapOf(TypeId.INT to int))

        assertSame(int, registry["int"])
        assertSame(int, registry[Int::class.javaObjectType])
        assertTrue(Int::class.javaObjectType in registry)
        assertNull(registry["string"])
        assertNull(registry[String::class.java])
    }

    @Test
    fun `setting a known code replaces its registry slot`() {
        val registry = TestDataTypes(mutableMapOf(TypeId.INT to IntDataType()))
        val replacement = object : IntDataType() {}

        registry["int"] = replacement

        assertSame(replacement, registry["int"])
        assertEquals(1, registry.typesRegistry.size)
    }

    @Test
    fun `setting an unknown code fails without mutation`() {
        val initial = mutableMapOf<TypeId, DataType<*>>(TypeId.INT to IntDataType())
        val registry = TestDataTypes(initial)

        assertFailsWith<IllegalArgumentException> { registry["unknown"] = StringDataType() }
        assertEquals(setOf(TypeId.INT), registry.typesRegistry.keys)
    }

    private class TestDataTypes(
        override val typesRegistry: MutableMap<TypeId, DataType<*>>,
    ) : DataTypes()
}
