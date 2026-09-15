package io.github.heyhey123.xiaojieorm.queries

import io.github.heyhey123.xiaojieorm.condition.Condition
import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.result.WriteResult
import io.github.heyhey123.xiaojieorm.table.Table
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class QuerySnapshotTest {
    @Test
    fun `single-row write queries snapshot their values`() {
        val source = linkedMapOf<String, Any?>("name" to "before")
        val queries = listOf(
            TestInsertOne(source).values,
            TestInsertIfAbsent(source).values,
            TestUpdateById(1, source).values,
            TestUpsertById(1, source).values,
        )

        source["name"] = "after"
        source["extra"] = true

        queries.forEach { values ->
            assertEquals(mapOf("name" to "before"), values)
            assertNotSame(source, values)
        }
    }

    @Test
    fun `insert many snapshots outer list and every row`() {
        val first = linkedMapOf<String, Any?>("id" to 1, "name" to "first")
        val second = linkedMapOf<String, Any?>("id" to 2, "name" to "second")
        val source = mutableListOf<Map<String, Any?>>(first, second)
        val query = TestInsertMany(source)

        first["name"] = "changed"
        second.clear()
        source.clear()

        assertEquals(
            listOf(
                mapOf("id" to 1, "name" to "first"),
                mapOf("id" to 2, "name" to "second"),
            ),
            query.valuesList,
        )
        assertNotSame(source, query.valuesList)
        assertNotSame(first, query.valuesList[0])
        assertNotSame(second, query.valuesList[1])
    }

    @Test
    fun `update snapshots values and retains the supplied where clause`() {
        val values = linkedMapOf<String, Any?>("name" to "before")
        val where = WhereClause.All(false, listOf(Condition.Equals("id", 7)))
        val query = TestUpdate(values, 1, where)

        values["name"] = "after"

        assertEquals(mapOf("name" to "before"), query.values)
        assertNotSame(values, query.values)
        assertEquals(1, query.limit)
        assertSame(where, query.where)
    }

    @Test
    fun `where clause snapshots its condition list`() {
        val first = Condition.Equals("id", 1)
        val mutableConditions = mutableListOf<Condition>(first)
        val where = WhereClause.Any(false, mutableConditions)

        mutableConditions.clear()

        assertEquals(1, where.conditions.size)
        assertSame(first, where.conditions.single())
    }

    private class TestInsertOne(values: Map<String, Any?>) : InsertOne(values) {
        override suspend fun execute(table: Table) = WriteResult(0)
    }

    private class TestInsertIfAbsent(values: Map<String, Any?>) : InsertIfAbsent(values) {
        override suspend fun execute(table: Table) = WriteResult(0)
    }

    private class TestUpdateById(id: Any, values: Map<String, Any?>) : UpdateById(id, values) {
        override suspend fun execute(table: Table) = WriteResult(0)
    }

    private class TestUpsertById(id: Any, values: Map<String, Any?>) : UpsertById(id, values) {
        override suspend fun execute(table: Table) = WriteResult(0)
    }

    private class TestInsertMany(valuesList: List<Map<String, Any?>>) : InsertMany(valuesList) {
        override suspend fun execute(table: Table) = WriteResult(0)
    }

    private class TestUpdate(
        values: Map<String, Any?>,
        limit: Int?,
        where: WhereClause?,
    ) : Update(values, limit, where) {
        override suspend fun execute(table: Table) = WriteResult(0)
    }
}
