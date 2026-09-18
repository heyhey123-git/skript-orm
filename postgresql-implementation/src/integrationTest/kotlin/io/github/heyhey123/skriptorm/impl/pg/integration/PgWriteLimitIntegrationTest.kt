package io.github.heyhey123.skriptorm.impl.pg.integration

import io.github.heyhey123.skriptorm.condition.Condition
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The one write extension PostgreSQL is missing is `UPDATE ... LIMIT`, which this dialect writes as a
 * `ctid IN (SELECT ... LIMIT n)` subquery instead. That is the whole of the difference, so these tests
 * are about which rows it reaches rather than about syntax.
 */
class PgWriteLimitIntegrationTest : PgIntegrationTestBase() {

    @Test
    fun `update with a limit changes only that many rows`() = runBlocking<Unit> {
        seed(1, 2, 3, 4)

        val result = queries.update(linkedMapOf<String, Any?>("age" to 99), 2, null).execute(usersTable)

        assertEquals(2L, result.affectedCount)
        assertEquals(2, allUsers().count { it.age == 99 })
    }

    @Test
    fun `a limited update applies its filter first`() = runBlocking<Unit> {
        seed(1, 2, 3, 4)

        val result = queries.update(
            linkedMapOf<String, Any?>("age" to 99),
            1,
            WhereClause.All(false, listOf(Condition.GreaterThanOrEquals("age", 30)))
        ).execute(usersTable)

        assertEquals(1L, result.affectedCount)
        assertEquals(1, allUsers().count { it.age == 99 })
        // The rows the filter excludes keep their values: the limit cannot reach them.
        assertEquals(listOf(10, 20), allUsers().mapNotNull { it.age }.filter { it < 30 })
    }

    @Test
    fun `delete with a limit removes only that many rows`() = runBlocking<Unit> {
        seed(1, 2, 3)

        val result = queries.delete(1, null).execute(usersTable)

        assertEquals(1L, result.affectedCount)
        assertEquals(2L, rawRowCount())
    }

    @Test
    fun `a limited delete applies its filter first`() = runBlocking<Unit> {
        seed(1, 2, 3, 4)

        val result = queries.delete(
            1,
            WhereClause.All(false, listOf(Condition.GreaterThanOrEquals("age", 30)))
        ).execute(usersTable)

        assertEquals(1L, result.affectedCount)
        assertEquals(3L, rawRowCount())
        assertEquals(listOf(10, 20), allUsers().mapNotNull { it.age })
    }

    @Test
    fun `a limited write needs no primary key`() = runBlocking<Unit> {
        val keyless = Table("orm_keyless", listOf(Column("value", intType), Column("label", stringType)))
        recreateTable(keyless)
        queries.insertMany(
            listOf(
                mapOf<String, Any?>("value" to 1, "label" to "a"),
                mapOf<String, Any?>("value" to 2, "label" to "b"),
                mapOf<String, Any?>("value" to 3, "label" to "c")
            )
        ).execute(keyless)

        val deleted = queries.delete(2, null).execute(keyless)
        val updated = queries.update(linkedMapOf<String, Any?>("label" to "changed"), 1, null).execute(keyless)

        assertEquals(2L, deleted.affectedCount)
        assertEquals(1L, updated.affectedCount)
        assertEquals(1L, rawRowCount(keyless.name))
    }

    private suspend fun seed(vararg ids: Int) {
        recreateTable()
        queries.insertMany(ids.map { userValues(id = it, name = "user-$it", age = it * 10) }).execute(usersTable)
    }
}
