package io.github.heyhey123.skriptorm.impl.jdbc.queries

import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.JdbcDataType
import io.github.heyhey123.skriptorm.type.StringDataType
import io.github.heyhey123.skriptorm.type.ValueConverter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.sql.Blob
import java.sql.JDBCType
import java.sql.PreparedStatement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class JdbcParameterBinderTest {

    @Test
    fun `null uses jdbc vendor type without conversion`() {
        val statement = mockk<PreparedStatement>(relaxed = true)

        statement.bindValue(2, null, IntJdbcDataType())

        verify(exactly = 1) { statement.setNull(2, JDBCType.INTEGER.vendorTypeNumber) }
        verify(exactly = 0) { statement.setObject(any(), any(), any<JDBCType>()) }
    }

    @Test
    fun `domain value is converted exactly once and bound with jdbc type`() {
        val statement = mockk<PreparedStatement>(relaxed = true)
        var conversions = 0
        val type = object : IntJdbcDataType() {
            override val converter = object : ValueConverter<Int, String>(Int::class.javaObjectType, String::class.java) {
                override fun toStorage(value: Int): String {
                    conversions++
                    return "stored:$value"
                }

                override fun fromStorage(value: String): Int = value.substringAfter(':').toInt()
            }
        }

        statement.bindValue(1, 7, type)

        assertEquals(1, conversions)
        verify { statement.setObject(1, "stored:7", JDBCType.INTEGER) }
    }

    @Test
    fun `unsupported data type and wrong domain value are rejected before jdbc call`() {
        val statement = mockk<PreparedStatement>(relaxed = true)

        assertFailsWith<IllegalArgumentException> { statement.bindValue(1, "x", StringDataType()) }
        assertFailsWith<IllegalArgumentException> { statement.bindValue(1, "x", IntJdbcDataType()) }
        verify(exactly = 0) { statement.setObject(any(), any(), any<JDBCType>()) }
    }

    @Test
    fun `bound blob is released after successful action`() {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val blob = mockk<Blob>(relaxed = true)
        val type = blobType(blob)

        statement.bindValue(1, "domain", type)
        val result = statement.withBoundResources { "done" }

        assertEquals("done", result)
        verify(exactly = 1) { blob.free() }
    }

    @Test
    fun `binding failure frees blob and suppresses cleanup failure`() {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val blob = mockk<Blob>()
        val bindFailure = IllegalStateException("bind")
        val freeFailure = IllegalStateException("free")
        every { statement.setObject(1, blob, JDBCType.BLOB) } throws bindFailure
        every { blob.free() } throws freeFailure

        val thrown = assertFailsWith<IllegalStateException> { statement.bindValue(1, "domain", blobType(blob)) }

        assertSame(bindFailure, thrown)
        assertEquals(listOf(freeFailure), thrown.suppressed.toList())
    }

    @Test
    fun `action failure remains primary when resource cleanup fails`() {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val blob = mockk<Blob>()
        val actionFailure = IllegalStateException("action")
        val cleanupFailure = IllegalStateException("cleanup")
        every { blob.free() } throws cleanupFailure
        statement.bindValue(1, "domain", blobType(blob))

        val thrown = assertFailsWith<IllegalStateException> {
            statement.withBoundResources { throw actionFailure }
        }

        assertSame(actionFailure, thrown)
        assertEquals(listOf(cleanupFailure), thrown.suppressed.toList())
    }

    @Test
    fun `release attempts every blob and aggregates cleanup failures`() {
        val statement = mockk<PreparedStatement>(relaxed = true)
        val first = mockk<Blob>()
        val second = mockk<Blob>()
        val firstFailure = IllegalStateException("first")
        val secondFailure = IllegalStateException("second")
        every { first.free() } throws firstFailure
        every { second.free() } throws secondFailure
        statement.bindValue(1, "a", blobType(first))
        statement.bindValue(2, "b", blobType(second))

        val thrown = assertFailsWith<IllegalStateException> { statement.releaseBoundResources() }

        assertSame(firstFailure, thrown)
        assertEquals(listOf(secondFailure), thrown.suppressed.toList())
        verify(exactly = 1) { first.free() }
        verify(exactly = 1) { second.free() }
        statement.releaseBoundResources()
    }

    private fun blobType(blob: Blob): JdbcDataType<String> = object : JdbcDataType<String> {
        override val domainType = String::class.java
        override val typeCode = "string"
        override val jdbcType = JDBCType.BLOB
        override val storageName = "BLOB"
        override val converter = object : ValueConverter<String, Blob>(String::class.java, Blob::class.java) {
            override fun toStorage(value: String): Blob = blob
            override fun fromStorage(value: Blob): String = "domain"
        }
    }
}
