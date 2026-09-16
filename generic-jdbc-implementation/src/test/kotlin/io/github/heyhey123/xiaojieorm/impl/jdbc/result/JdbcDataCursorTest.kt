package io.github.heyhey123.xiaojieorm.impl.jdbc.result

import io.github.heyhey123.xiaojieorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.sql.Blob
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class JdbcDataCursorTest {

    @Test
    fun `next delegates to result set`() {
        val resultSet = mockk<ResultSet>()
        every { resultSet.next() } returns true
        val cursor = cursor(resultSet = resultSet)

        assertEquals(true, cursor.next())
    }

    @Test
    fun `get by name and index requests boxed storage class and converts`() {
        val resultSet = mockk<ResultSet>()
        every { resultSet.getObject("ticks", Int::class.javaObjectType) } returns 40
        every { resultSet.getObject(2, Int::class.javaObjectType) } returns 60
        every { resultSet.wasNull() } returns false
        val cursor = cursor(resultSet = resultSet)
        val dataType = offsetIntType()

        assertEquals(41, cursor.get("ticks", dataType))
        assertEquals(61, cursor.get(2, dataType))
    }

    @Test
    fun `sql null bypasses converter`() {
        val resultSet = mockk<ResultSet>()
        every { resultSet.getObject("value", Int::class.javaObjectType) } returns 0
        every { resultSet.wasNull() } returns true
        val cursor = cursor(resultSet = resultSet)

        assertNull(cursor.get("value", IntJdbcDataType()))
    }

    @Test
    fun `blob storage is read with the blob accessor by name and index`() {
        // Connector/J rejects a typed getObject for Blob, so a Blob-backed column has to be read with
        // the dedicated accessor. Without this, every row carrying one is unreadable.
        val resultSet = mockk<ResultSet>()
        val blob = mockk<Blob>()
        every { resultSet.getBlob("payload") } returns blob
        every { resultSet.getBlob(3) } returns blob
        every { resultSet.wasNull() } returns false
        val cursor = cursor(resultSet = resultSet)
        val dataType = blobType()

        assertSame(blob, cursor.get("payload", dataType))
        assertSame(blob, cursor.get(3, dataType))
        verify(exactly = 0) { resultSet.getObject(any<String>(), any<Class<*>>()) }
        verify(exactly = 0) { resultSet.getObject(any<Int>(), any<Class<*>>()) }
    }

    @Test
    fun `a null blob column reads as null without reaching the converter`() {
        val resultSet = mockk<ResultSet>()
        every { resultSet.getBlob("payload") } returns null
        every { resultSet.wasNull() } returns true
        val cursor = cursor(resultSet = resultSet)

        assertNull(cursor.get("payload", blobType()))
    }

    @Test
    fun `close releases all resources once in ownership order`() {
        val events = mutableListOf<String>()
        val resultSet = mockk<ResultSet>()
        val statement = mockk<Statement>()
        val connection = mockk<Connection>()
        every { resultSet.close() } answers { events += "resultSet" }
        every { statement.close() } answers { events += "statement" }
        every { connection.close() } answers { events += "connection" }
        val cursor = JdbcDataCursor(resultSet, statement, connection) { events += "bound" }

        cursor.close()
        cursor.close()

        assertEquals(listOf("bound", "resultSet", "statement", "connection"), events)
        verify(exactly = 1) { resultSet.close() }
        verify(exactly = 1) { statement.close() }
        verify(exactly = 1) { connection.close() }
    }

    @Test
    fun `close attempts all resources and suppresses later failures`() {
        val resultSet = mockk<ResultSet>()
        val statement = mockk<Statement>()
        val connection = mockk<Connection>()
        val boundFailure = IllegalStateException("bound")
        val resultFailure = IllegalStateException("result")
        val statementFailure = IllegalStateException("statement")
        val connectionFailure = IllegalStateException("connection")
        every { resultSet.close() } throws resultFailure
        every { statement.close() } throws statementFailure
        every { connection.close() } throws connectionFailure
        val cursor = JdbcDataCursor(resultSet, statement, connection) { throw boundFailure }

        val thrown = assertFailsWith<IllegalStateException> { cursor.close() }

        assertSame(boundFailure, thrown)
        assertEquals(listOf(resultFailure, statementFailure, connectionFailure), thrown.suppressed.toList())
        verify { resultSet.close() }
        verify { statement.close() }
        verify { connection.close() }
    }

    private fun blobType(): DataType<Any> = object : DataType<Any> {
        override val domainType = Any::class.java
        override val typeCode = "blob"
        override val converter = object : ValueConverter<Any, Blob>(Any::class.java, Blob::class.java) {
            override fun toStorage(value: Any): Blob = error("Reading only.")
            override fun fromStorage(value: Blob): Any = value
        }
    }

    private fun offsetIntType(): DataType<Int> = object : DataType<Int> {
        override val domainType = Int::class.javaObjectType
        override val typeCode = "offset-int"
        override val converter = object : ValueConverter<Int, Int>(Int::class.javaObjectType, Int::class.java) {
            override fun toStorage(value: Int): Int = value - 1
            override fun fromStorage(value: Int): Int = value + 1
        }
    }

    private fun cursor(
        resultSet: ResultSet = mockk(relaxed = true),
        statement: Statement = mockk(relaxed = true),
        connection: Connection = mockk(relaxed = true)
    ) = JdbcDataCursor(resultSet, statement, connection)
}
