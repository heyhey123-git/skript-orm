package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.condition.Condition
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.result.DataCursor
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.DataType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class QueryContractTest {

    private val where = WhereClause.All(false, listOf(Condition.Equals("id", 1)))

    @Test
    fun `read and delete queries retain constructor arguments`() {
        val id = Any()
        assertSame(id, TestSelectById(id).id)
        assertSame(id, TestDeleteById(id).id)
        assertSame(where, TestSelectOne(where).where)
        assertSame(where, TestSelectMany(where).where)
        val delete = TestDelete(3, where)
        assertEquals(3, delete.limit)
        assertSame(where, delete.where)
    }

    @Test
    fun `page query retains valid arguments and clause`() {
        val query = TestSelectPage(20, 2, where)
        assertEquals(20, query.pageSize)
        assertEquals(2, query.pageIndex)
        assertSame(where, query.where)
    }

    @Test
    fun `page query rejects non-positive size and index`() {
        listOf(0, -1).forEach { size ->
            assertFailsWith<IllegalArgumentException> { TestSelectPage(size, 1, null) }
        }
        listOf(0, -1).forEach { index ->
            assertFailsWith<IllegalArgumentException> { TestSelectPage(1, index, null) }
        }
    }

    private class EmptyCursor : DataCursor {

        override fun next() = false
        override fun <T : Any> get(column: String, dataType: DataType<T>): T? = null
        override fun <T : Any> get(index: Int, dataType: DataType<T>): T? = null
        override fun close() = Unit
    }

    private class TestSelectById(id: Any) : SelectById(id) {

        override suspend fun execute(table: Table) = CursorResult(EmptyCursor())
    }

    private class TestDeleteById(id: Any) : DeleteById(id) {

        override suspend fun execute(table: Table) = WriteResult(0)
    }

    private class TestSelectOne(where: WhereClause?) : SelectOne(where) {

        override suspend fun execute(table: Table) = CursorResult(EmptyCursor())
    }

    private class TestSelectMany(where: WhereClause?) : SelectMany(where) {

        override suspend fun execute(table: Table) = CursorResult(EmptyCursor())
    }

    private class TestSelectPage(size: Int, index: Int, where: WhereClause?) : SelectPage(size, index, where) {

        override suspend fun execute(table: Table) = CursorResult(EmptyCursor())
    }

    private class TestDelete(limit: Int?, where: WhereClause?) : Delete(limit, where) {

        override suspend fun execute(table: Table) = WriteResult(0)
    }
}
