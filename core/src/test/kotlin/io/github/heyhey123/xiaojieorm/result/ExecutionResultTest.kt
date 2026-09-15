package io.github.heyhey123.xiaojieorm.result

import io.github.heyhey123.xiaojieorm.type.DataType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ExecutionResultTest {

    @Test
    fun `cursor result exposes only cursor projection`() {
        val cursor = EmptyCursor()
        val result = CursorResult(cursor)

        assertSame(cursor, result.asCursorOrNull())
        assertNull(result.asWriteResultOrNull())
    }

    @Test
    fun `write result exposes only write projection and exactness`() {
        val exact = WriteResult(3)
        val inexact = WriteResult(2, countExact = false)

        assertNull(exact.asCursorOrNull())
        assertSame(exact, exact.asWriteResultOrNull())
        assertEquals(3, exact.affectedCount)
        assertTrue(exact.countExact)
        assertFalse(inexact.countExact)
    }

    private class EmptyCursor : DataCursor {

        override fun next() = false
        override fun <T : Any> get(column: String, dataType: DataType<T>): T? = null
        override fun <T : Any> get(index: Int, dataType: DataType<T>): T? = null
        override fun close() = Unit
    }
}
