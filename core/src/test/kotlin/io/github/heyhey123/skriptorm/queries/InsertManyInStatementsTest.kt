package io.github.heyhey123.skriptorm.queries

import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.skript.utils.RowLimit
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.IntDataType
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Where a write's statement boundaries fall, and what a batch made of several statements reports.
 *
 * A backend that records the batches it is handed is enough: the boundary is a number, the interesting cases
 * are the ones on either side of it, and what the caller is promised is that every row leaves this side
 * exactly once and in order. Whether a driver then accepts a batch of that size is a query-layer question,
 * covered against real servers.
 */
class InsertManyInStatementsTest {

    @Test
    fun `a batch within the boundary is one statement, handed over as it is`() = runBlocking {
        val rows = rows(RowLimit.rowsPerStatement(COLUMNS))
        val queries = RecordingQueries()

        val result = queries.insertManyInStatements(table(COLUMNS), rows)

        assertEquals(1, queries.statements.size, "a batch that fits is one statement: ${queries.statements.size}")
        assertSame(rows, queries.statements.single(), "a batch that fits is not copied on the way out")
        assertEquals(rows.size.toLong(), result.affectedCount)
        assertTrue(result.countExact)
    }

    @Test
    fun `a batch past the boundary is split so that every row is still written`() = runBlocking {
        val boundary = RowLimit.rowsPerStatement(COLUMNS)
        val given = boundary * 2 + 7
        val rows = rows(given)
        val queries = RecordingQueries()

        val result = queries.insertManyInStatements(table(COLUMNS), rows)

        assertEquals(3, queries.statements.size, "a batch of $given rows is three statements")
        assertTrue(
            queries.statements.all { it.size <= boundary },
            "every statement stays within the boundary: ${queries.statements.map { it.size }}"
        )
        assertEquals(rows, queries.statements.flatten(), "every row is written, once, in the order it was given")
        assertEquals(given.toLong(), result.affectedCount)
        assertTrue(result.countExact)
    }

    @Test
    fun `a wider table moves fewer rows per statement`() = runBlocking {
        val given = 2000
        val boundary = RowLimit.rowsPerStatement(WIDE_COLUMNS)
        val rows = rows(given)
        val queries = RecordingQueries()

        queries.insertManyInStatements(table(WIDE_COLUMNS), rows)

        assertTrue(boundary < given, "the case is only interesting while the batch crosses the boundary")
        assertEquals(listOf(boundary, given - boundary), queries.statements.map { it.size })
        assertEquals(rows, queries.statements.flatten())
    }

    @Test
    fun `one inexact statement makes the whole count inexact`() = runBlocking {
        val rows = rows(RowLimit.rowsPerStatement(COLUMNS) + 1)
        val queries = RecordingQueries(exact = false)

        val result = queries.insertManyInStatements(table(COLUMNS), rows)

        assertEquals(2, queries.statements.size)
        assertEquals(rows.size.toLong(), result.affectedCount)
        assertFalse(result.countExact, "the boundary does not invent an exact count the backend did not give")
    }

    private fun table(columns: Int) =
        Table("users", List(columns) { index -> Column("c$index", IntDataType()) })

    private fun rows(count: Int) = List(count) { index -> mapOf<String, Any?>("c0" to index) }

    /** A backend that keeps every batch it is asked to write, and reports the rows as written. */
    private class RecordingQueries(private val exact: Boolean = true) : Queries {

        val statements = mutableListOf<List<Map<String, Any?>>>()

        override val typeName: String = "Recording"

        override fun insertMany(valuesList: List<Map<String, Any?>>): InsertMany {
            statements += valuesList
            return RecordingInsertMany(valuesList, exact)
        }

        override fun selectById(id: Any): SelectById = unused()
        override fun selectOne(where: WhereClause?): SelectOne = unused()
        override fun selectMany(where: WhereClause?): SelectMany = unused()
        override fun selectMany(where: WhereClause?, limit: Int): SelectMany = unused()
        override fun selectPage(pageSize: Int, pageIndex: Int, where: WhereClause?): SelectPage = unused()
        override fun insertOne(values: Map<String, Any?>): InsertOne = unused()
        override fun insertIfAbsent(values: Map<String, Any?>): InsertIfAbsent = unused()
        override fun update(values: Map<String, Any?>, limit: Int?, where: WhereClause?): Update = unused()
        override fun updateById(id: Any, values: Map<String, Any?>): UpdateById = unused()
        override fun upsertById(id: Any, values: Map<String, Any?>): UpsertById = unused()
        override fun delete(limit: Int?, where: WhereClause?): Delete = unused()
        override fun deleteById(id: Any): DeleteById = unused()

        private fun <T> unused(): T = throw UnsupportedOperationException("only insertMany is used here")
    }

    private class RecordingInsertMany(
        valuesList: List<Map<String, Any?>>,
        private val exact: Boolean
    ) : InsertMany(valuesList) {

        override suspend fun execute(table: Table) = WriteResult(valuesList.size.toLong(), exact)
    }

    private companion object {

        /** Six columns is the shape the value budget was measured on, so it is the one a boundary case uses. */
        const val COLUMNS = 6

        /** Enough columns that the boundary falls well inside the batch a test can afford to build. */
        const val WIDE_COLUMNS = 20
    }
}
