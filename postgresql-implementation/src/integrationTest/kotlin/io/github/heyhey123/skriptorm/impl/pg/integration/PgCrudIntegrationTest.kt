package io.github.heyhey123.skriptorm.impl.pg.integration

import io.github.heyhey123.skriptorm.condition.Condition
import io.github.heyhey123.skriptorm.condition.WhereClause
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises every concrete query against PostgreSQL. The dialect unit tests pin the SQL that is
 * rendered; these confirm that PostgreSQL accepts it and returns the rows and counts the core API
 * promises.
 */
class PgCrudIntegrationTest : PgIntegrationTestBase() {

    @Test
    fun `insertOne writes one row that selectById finds`() = runBlocking<Unit> {
        recreateTable()

        val result = queries.insertOne(userValues(id = 1, name = "alice", age = 30)).execute(usersTable)

        assertEquals(1L, result.affectedCount)
        assertTrue(result.countExact)
        assertEquals(listOf(1), queries.selectById(1).execute(usersTable).readUsers().map { it.id })
        assertEquals(1L, rawRowCount())
    }

    @Test
    fun `an insert without an id lets the identity column generate one`() = runBlocking<Unit> {
        recreateTable()

        // A fresh table, and no explicit id anywhere in this test: PostgreSQL advances its identity
        // sequence only for the values it generates, so mixing the two can collide on the same key.
        queries.insertOne(userValues(name = "first")).execute(usersTable)
        queries.insertOne(userValues(name = "second")).execute(usersTable)

        assertEquals(listOf(1, 2), allUsers().map { it.id })
    }

    @Test
    fun `every stored type comes back unchanged`() = runBlocking<Unit> {
        recreateTable()
        val uid = UUID.randomUUID()

        queries.insertOne(
            userValues(
                name = "typed",
                age = 42,
                score = 1.5,
                ratio = 0.5f,
                big = 9_000_000_000L,
                tiny = 7,
                active = true,
                uid = uid
            )
        ).execute(usersTable)

        val row = allUsers().single()
        assertEquals(42, row.age)
        assertEquals(1.5, row.score)
        assertEquals(0.5f, row.ratio)
        assertEquals(9_000_000_000L, row.big)
        assertEquals(7.toByte(), row.tiny)
        assertEquals(true, row.active)
        assertEquals(uid, row.uid)
    }

    @Test
    fun `a null column stays null`() = runBlocking<Unit> {
        recreateTable()

        queries.insertOne(userValues(name = "empty")).execute(usersTable)

        val row = allUsers().single()
        assertNull(row.age)
        assertNull(row.score)
        assertNull(row.ratio)
        assertNull(row.big)
        assertNull(row.tiny)
        assertNull(row.active)
        assertNull(row.uid)
    }

    @Test
    fun `selectById returns nothing for an unknown identifier`() = runBlocking<Unit> {
        recreateTable()

        assertTrue(queries.selectById(404).execute(usersTable).readUsers().isEmpty())
    }

    @Test
    fun `selectOne stops after the first match`() = runBlocking<Unit> {
        seed(1, 2, 3)

        val first = queries.selectOne(
            WhereClause.All(false, listOf(Condition.GreaterThan("age", 0)))
        ).execute(usersTable).readUsers()
        val none = queries.selectOne(
            WhereClause.All(false, listOf(Condition.Equals("age", -1)))
        ).execute(usersTable).readUsers()

        assertEquals(1, first.size)
        assertTrue(none.isEmpty())
    }

    @Test
    fun `selectMany applies and or between and negation`() = runBlocking<Unit> {
        recreateTable()
        queries.insertMany(
            listOf(
                userValues(id = 1, name = "a", age = 10),
                userValues(id = 2, name = "b", age = null),
                userValues(id = 3, name = "c", age = 30)
            )
        ).execute(usersTable)

        val withinRange = WhereClause.All(
            false,
            listOf(Condition.GreaterThanOrEquals("age", 20), Condition.LessThan("age", 40))
        )
        val nullOrLarge = WhereClause.Any(
            false,
            listOf(Condition.Equals("age", null), Condition.GreaterThan("age", 25))
        )
        val between = WhereClause.All(false, listOf(Condition.Between("age", 10, 30)))
        val notA = WhereClause.All(true, listOf(Condition.Equals("name", "a")))

        assertEquals(listOf(3), matchedIds(withinRange))
        assertEquals(listOf(2, 3), matchedIds(nullOrLarge))
        assertEquals(listOf(1, 3), matchedIds(between))
        assertEquals(listOf(2, 3), matchedIds(notA))
    }

    @Test
    fun `selectPage walks the ordered pages`() = runBlocking<Unit> {
        seed(1, 2, 3, 4, 5)

        assertEquals(listOf(1, 2), pageIds(2, 1))
        assertEquals(listOf(3, 4), pageIds(2, 2))
        assertEquals(listOf(5), pageIds(2, 3))
        assertTrue(pageIds(2, 4).isEmpty(), "a page past the end must be empty")
    }

    @Test
    fun `selectPage applies the filter before paginating`() = runBlocking<Unit> {
        seed(1, 2, 3, 4, 5)
        val adults = WhereClause.All(false, listOf(Condition.GreaterThanOrEquals("age", 20)))

        assertEquals(listOf(2, 3, 4, 5), pageIds(4, 1, adults))
        assertEquals(listOf(3), pageIds(1, 2, adults))
    }

    @Test
    fun `selectPage refuses a table without a primary key`() = runBlocking<Unit> {
        val keyless = Table("orm_keyless", listOf(Column("value", intType)))
        recreateTable(keyless)

        assertFailsWith<IllegalArgumentException> {
            queries.selectPage(1, 1, null).execute(keyless)
        }
    }

    @Test
    fun `insertMany writes every row with an exact count`() = runBlocking<Unit> {
        recreateTable()

        val result = queries.insertMany(
            listOf(userValues(id = 1, name = "a"), userValues(id = 2, name = "b"))
        ).execute(usersTable)

        assertEquals(2L, result.affectedCount)
        assertTrue(result.countExact)
        assertEquals(listOf(1, 2), allUsers().map { it.id })
    }

    @Test
    fun `insertMany with no rows writes nothing`() = runBlocking<Unit> {
        recreateTable()

        val result = queries.insertMany(emptyList()).execute(usersTable)

        assertEquals(0L, result.affectedCount)
        assertEquals(0L, rawRowCount())
    }

    @Test
    fun `insertIfAbsent keeps the first row for a primary key`() = runBlocking<Unit> {
        recreateTable()
        queries.insertOne(userValues(id = 1, name = "first")).execute(usersTable)

        queries.insertIfAbsent(userValues(id = 1, name = "second")).execute(usersTable)
        queries.insertIfAbsent(userValues(id = 2, name = "third")).execute(usersTable)

        assertEquals(listOf(1 to "first", 2 to "third"), allUsers().map { it.id to it.name })
    }

    @Test
    fun `update honours the where clause`() = runBlocking<Unit> {
        seed(1, 2, 3, 4)

        val result = queries.update(
            linkedMapOf<String, Any?>("age" to 99),
            null,
            WhereClause.All(false, listOf(Condition.GreaterThanOrEquals("age", 30)))
        ).execute(usersTable)

        assertEquals(2L, result.affectedCount)
        assertEquals(listOf(10, 20, 99, 99), allUsers().map { it.age })
    }

    @Test
    fun `updateById changes exactly the addressed row`() = runBlocking<Unit> {
        seed(1, 2, 3)

        val result = queries.updateById(2, mapOf<String, Any?>("name" to "renamed")).execute(usersTable)

        assertEquals(1L, result.affectedCount)
        assertEquals(listOf("user-1", "renamed", "user-3"), allUsers().map { it.name })
    }

    @Test
    fun `upsertById inserts and then updates the same primary key`() = runBlocking<Unit> {
        recreateTable()

        val inserted = queries.upsertById(5, mapOf<String, Any?>("name" to "created", "age" to 1))
            .execute(usersTable)
        val updated = queries.upsertById(5, mapOf<String, Any?>("name" to "changed", "age" to 2))
            .execute(usersTable)

        assertEquals(1L, inserted.affectedCount)
        assertEquals(1L, updated.affectedCount)
        assertEquals(listOf(5 to "changed"), allUsers().map { it.id to it.name })
        assertEquals(listOf(2), allUsers().map { it.age })
    }

    @Test
    fun `delete removes the matching rows`() = runBlocking<Unit> {
        seed(1, 2, 3)

        val result = queries.delete(
            null,
            WhereClause.All(false, listOf(Condition.LessThan("age", 30)))
        ).execute(usersTable)

        assertEquals(2L, result.affectedCount)
        assertEquals(listOf(3), allUsers().map { it.id })
    }

    @Test
    fun `deleteById removes one row`() = runBlocking<Unit> {
        seed(1, 2, 3)

        val result = queries.deleteById(2).execute(usersTable)

        assertEquals(1L, result.affectedCount)
        assertEquals(listOf(1, 3), allUsers().map { it.id })
    }

    @Test
    fun `writes report zero when nothing matches`() = runBlocking<Unit> {
        seed(1, 2)

        assertEquals(0L, queries.deleteById(99).execute(usersTable).affectedCount)
        assertEquals(0L, queries.updateById(99, mapOf<String, Any?>("age" to 1)).execute(usersTable).affectedCount)
        assertEquals(
            0L,
            queries.delete(null, WhereClause.All(false, listOf(Condition.Equals("name", "absent"))))
                .execute(usersTable).affectedCount
        )
    }

    private suspend fun seed(vararg ids: Int) {
        recreateTable()
        queries.insertMany(ids.map { userValues(id = it, name = "user-$it", age = it * 10) }).execute(usersTable)
    }

    private suspend fun matchedIds(where: WhereClause?): List<Int> =
        queries.selectMany(where).execute(usersTable).readUsers().map { it.id }.sorted()

    private suspend fun pageIds(pageSize: Int, pageIndex: Int, where: WhereClause? = null): List<Int> =
        queries.selectPage(pageSize, pageIndex, where).execute(usersTable).readUsers().map { it.id }
}
