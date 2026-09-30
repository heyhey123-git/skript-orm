package io.github.heyhey123.skriptorm.impl.jdbc.integration

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The MariaDB specific promise of the implementation: a `"MariaDB"` connection serves the same table
 * work as MySQL, and a batch insert reports an exact affected-row count.
 *
 * The count is the reason this type exists. Connector/J leaves batching off unless it is asked for it,
 * while MariaDB Connector/J sends a prepared insert batch as one bulk command and counts every row,
 * so the same Skript code writes the same rows on MariaDB without asking for anything.
 *
 * The tests that only differ by which server answers them, such as the schema and the type round
 * trips, are shared through [MysqlIntegrationTestBase] and run against MariaDB by their `Mariadb*`
 * subclasses rather than being repeated here.
 */
class MariadbIntegrationTest : MysqlIntegrationTestBase() {

    override val product = MysqlTestServer.Product.MARIADB

    @Test
    fun `insertMany writes a large batch with an exact count`() = runBlocking<Unit> {
        recreateTable()
        val rows = (1..1_000).map { userValues(id = it, name = "batch-$it") }

        val result = queries.insertMany(rows).execute(usersTable)

        assertEquals(1_000L, result.affectedCount)
        assertTrue(result.countExact, "MariaDB reported an inexact affected count for a batch insert.")
        assertEquals(1_000L, rawRowCount())
    }

    @Test
    fun `a duplicate primary key reads as a row that is already present`() = runBlocking<Unit> {
        recreateTable()
        queries.insertOne(userValues(id = 1, name = "first")).execute(usersTable)

        val written = queries.insertIfAbsent(userValues(id = 1, name = "second")).execute(usersTable)

        assertEquals(0L, written.affectedCount)
        assertEquals(listOf("first"), allUsers().map { it.name })
    }

    @Test
    fun `the ordinary query path works through the mariadb connector`() = runBlocking<Unit> {
        recreateTable()
        queries.insertOne(userValues(id = 1, name = "first", age = 20)).execute(usersTable)
        queries.insertOne(userValues(id = 2, name = "second", age = 30)).execute(usersTable)

        val updated = queries.updateById(1, mapOf<String, Any?>("name" to "renamed")).execute(usersTable)
        assertEquals(1L, updated.affectedCount)

        val deleted = queries.deleteById(2).execute(usersTable)
        assertEquals(1L, deleted.affectedCount)

        assertEquals(listOf(1 to "renamed"), allUsers().map { it.id to it.name })
        assertEquals(1L, rawRowCount())
    }
}
