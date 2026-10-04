package io.github.heyhey123.skriptorm.type

import kotlinx.coroutines.runBlocking
import java.sql.Blob
import javax.sql.rowset.serial.SerialBlob
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class StorageValuesTest {
    @Test
    fun `unknown conversions are submitted together and are not repeated`() = runBlocking {
        var submissions = 0
        var conversions = 0
        val type = object : DataType<String> {
            override val domainType = String::class.java
            override val typeCode = "custom"
            override val converter = object : ValueConverter<String, String>(domainType, domainType) {
                override fun toStorage(value: String): String {
                    conversions++
                    return "stored:$value"
                }
                override fun fromStorage(value: String): String = value
            }
        }
        ConversionWork.install(object : ConversionWork.Executor {
            override suspend fun <T> executeBatch(actions: List<() -> T>): List<T> {
                submissions++
                assertEquals(3, actions.size)
                return actions.map { it() }
            }
        })
        try {
            assertEquals(ConversionThread.SERVER, type.converter.writeThread)
            val values = StorageValues.prepareBatch(listOf(type to "a", type to "b", type to "c"))
            assertEquals(1, submissions)
            assertEquals(3, conversions)
            assertEquals("stored:b", assertIs<PreparedDatabaseValue>(values[1]).storageValue())
            StorageValues.prepareBatch(values.map { type to it })
            assertEquals(1, submissions)
            assertEquals(3, conversions)
        } finally {
            ConversionWork.install(null)
        }
    }

    @Test
    fun `prepared blobs release conversion resources and create independent binding resources`() = runBlocking {
        val original = SerialBlob(byteArrayOf(1, 2, 3))
        val type = object : DataType<String> {
            override val domainType = String::class.java
            override val typeCode = "blob"
            override val converter = object : ValueConverter<String, Blob>(domainType, Blob::class.java) {
                override fun toStorage(value: String): Blob = original
                override fun fromStorage(value: Blob): String = error("Unused")
            }
        }
        val prepared = assertIs<PreparedDatabaseValue>(StorageValues.prepare(type, "value"))
        assertFailsWith<java.sql.SQLException> { original.length() }
        val first = prepared.storageValue() as Blob
        val second = prepared.storageValue() as Blob
        first.free()
        assertContentEquals(byteArrayOf(1, 2, 3), second.getBytes(1, second.length().toInt()))
        second.free()
    }
}
