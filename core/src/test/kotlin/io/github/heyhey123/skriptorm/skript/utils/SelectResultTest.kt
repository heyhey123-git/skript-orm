package io.github.heyhey123.skriptorm.skript.utils

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.queries.Delete
import io.github.heyhey123.skriptorm.queries.DeleteById
import io.github.heyhey123.skriptorm.queries.InsertIfAbsent
import io.github.heyhey123.skriptorm.queries.InsertMany
import io.github.heyhey123.skriptorm.queries.InsertOne
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.queries.SelectById
import io.github.heyhey123.skriptorm.queries.SelectMany
import io.github.heyhey123.skriptorm.queries.SelectOne
import io.github.heyhey123.skriptorm.queries.SelectPage
import io.github.heyhey123.skriptorm.queries.Update
import io.github.heyhey123.skriptorm.queries.UpdateById
import io.github.heyhey123.skriptorm.queries.UpsertById
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.result.DataCursor
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.DataType
import io.github.heyhey123.skriptorm.type.IntDataType
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Where the read ceiling falls, and the one row that decides it.
 *
 * A fake backend that answers a select with as many rows as it holds is enough for this: the boundary is a
 * number, and the interesting cases are the two that sit on either side of it. Whether the ceiling reaches
 * the server at all is a query-layer question, covered against real servers.
 */
class SelectResultTest {

    private val table = Table("users", listOf(Column("id", IntDataType())))

    @Test
    fun `a result of exactly the ceiling is stored whole`() = runBlocking<Unit> {
        val queries = FakeQueries(rows = RowLimit.ROWS)

        val stored = SelectResult.readMany(queries, table, null)

        assertEquals(RowLimit.PROBE_ROWS, queries.askedFor, "the server is asked for one row past the ceiling")
        assertEquals(RowLimit.ROWS, stored.size)
        assertEquals(RowLimit.ROWS, stored.keys.count { it.endsWith("::id") })
        assertEquals(1, stored["1::id"], "row indexes are one-based")
        assertEquals(RowLimit.ROWS, stored["${RowLimit.ROWS}::id"])
    }

    @Test
    fun `one row past the ceiling refuses and stores nothing`() = runBlocking<Unit> {
        val queries = FakeQueries(rows = RowLimit.ROWS + 1)

        val thrown = assertFailsWith<TooManyRowsException> {
            SelectResult.readMany(queries, table, null)
        }

        assertEquals(RowLimit.readRefusal("users"), thrown.refusal)
    }

    @Test
    fun `a table smaller than the ceiling is read to its end`() = runBlocking<Unit> {
        val queries = FakeQueries(rows = 3)

        val stored = SelectResult.readMany(queries, table, null)

        assertEquals(3, stored.size)
        assertTrue("4::id" !in stored, "a row that is not there is not stored: ${stored.keys}")
    }

    private class FakeQueries(private val rows: Int) : Queries {

        /** The ceiling the reader handed to this backend, so the probe can be asserted. */
        var askedFor: Int? = null

        override val typeName: String = "Fake"

        override fun selectMany(where: WhereClause?): SelectMany = selectMany(where, rows)

        override fun selectMany(where: WhereClause?, limit: Int): SelectMany {
            askedFor = limit
            return FakeSelectMany(where, limit, minOf(rows, limit))
        }

        override fun selectById(id: Any): SelectById = unused()
        override fun selectOne(where: WhereClause?): SelectOne = unused()
        override fun selectPage(pageSize: Int, pageIndex: Int, where: WhereClause?): SelectPage = unused()
        override fun insertOne(values: Map<String, Any?>): InsertOne = unused()
        override fun insertMany(valuesList: List<Map<String, Any?>>): InsertMany = unused()
        override fun insertIfAbsent(values: Map<String, Any?>): InsertIfAbsent = unused()
        override fun update(values: Map<String, Any?>, limit: Int?, where: WhereClause?): Update = unused()
        override fun updateById(id: Any, values: Map<String, Any?>): UpdateById = unused()
        override fun upsertById(id: Any, values: Map<String, Any?>): UpsertById = unused()
        override fun delete(limit: Int?, where: WhereClause?): Delete = unused()
        override fun deleteById(id: Any): DeleteById = unused()

        private fun <T> unused(): T = throw UnsupportedOperationException("only selectMany is used here")
    }

    private class FakeSelectMany(
        where: WhereClause?,
        limit: Int?,
        private val rows: Int
    ) : SelectMany(where, limit) {

        override suspend fun execute(table: Table) = CursorResult(FakeCursor(rows))
    }

    /** Answers [rows] rows, each holding its own one-based index in `id`. */
    private class FakeCursor(private val rows: Int) : DataCursor {

        private var position = 0

        override fun next(): Boolean = if (position < rows) {
            position++
            true
        } else {
            false
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> get(column: String, dataType: DataType<T>): T = position as T

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> get(index: Int, dataType: DataType<T>): T = position as T

        override fun close() = Unit
    }
}
