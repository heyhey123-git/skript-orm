package io.github.heyhey123.skriptorm.impl.pg.integration

import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Checks the table PostgreSQL ends up with. The declarations are this implementation's decision — a
 * `tinyint` is a `SMALLINT`, a `float` is a `REAL`, a UUID is `BYTEA` — so they are read back from
 * `information_schema` instead of being assumed.
 */
class PgSchemaIntegrationTest : PgIntegrationTestBase() {

    @Test
    fun `registering a table stores every type under its PostgreSQL name`() = runBlocking<Unit> {
        recreateTable()

        val columns = columnsOf()

        assertEquals("integer", columns.getValue("id").dataType)
        assertEquals("bigint", columns.getValue("big").dataType)
        assertEquals("smallint", columns.getValue("tiny").dataType)
        assertEquals("real", columns.getValue("ratio").dataType)
        assertEquals("double precision", columns.getValue("score").dataType)
        assertEquals("boolean", columns.getValue("active").dataType)
        assertEquals("character varying", columns.getValue("name").dataType)
        assertEquals("bytea", columns.getValue("uid").dataType)
    }

    @Test
    fun `registering a table records nullability the size and the identity key`() = runBlocking<Unit> {
        recreateTable()

        val columns = columnsOf()

        assertFalse(columns.getValue("id").nullable)
        assertTrue(columns.getValue("id").identity)
        assertFalse(columns.getValue("name").nullable)
        assertEquals(100, columns.getValue("name").size)
        assertTrue(columns.getValue("age").nullable)

        assertEquals(listOf("id"), primaryKeys())
    }

    @Test
    fun `registering a table twice leaves the first one in place`() = runBlocking<Unit> {
        recreateTable()
        queries.insertOne(userValues(id = 1, name = "kept")).execute(usersTable)

        // Registering is `CREATE TABLE IF NOT EXISTS`, so the statement is accepted and the rows with
        // it: the table is already there and the declaration is not applied again.
        database.registerTable(usersTable)

        assertEquals(1L, rawRowCount())
        assertEquals(listOf("kept"), allUsers().map { it.name })
    }

    @Test
    fun `a keyless table is created as declared`() = runBlocking<Unit> {
        val keyless = Table(
            "orm_keyless",
            listOf(Column("value", intType), Column("label", stringType, size = 20, isNullable = true))
        )

        recreateTable(keyless)

        assertTrue(primaryKeys(keyless.name).isEmpty())
        assertEquals("integer", columnsOf(keyless.name).getValue("value").dataType)
        assertEquals(20, columnsOf(keyless.name).getValue("label").size)
    }

    /**
     * The same comparison the MySQL suite makes, against PostgreSQL's own metadata: its driver reports
     * `int4`, `varchar` and `bool` where the declaration says `INT`, `VARCHAR` and `BOOLEAN`, so this is
     * also what proves the two spellings are recognised as the same storage rather than reported as a
     * difference.
     */
    @Test
    fun `a declaration the existing table does not match is refused at registration`() = runBlocking<Unit> {
        executeSql("DROP TABLE IF EXISTS \"orm_older_users\"")
        executeSql(
            "CREATE TABLE \"orm_older_users\" (\"id\" INTEGER NOT NULL PRIMARY KEY, " +
                "\"name\" VARCHAR(64) NOT NULL)"
        )
        val withAge = Table(
            "orm_older_users",
            listOf(
                Column("id", intType, isPrimaryKey = true, isNullable = false),
                Column("name", stringType, isNullable = false, size = 64),
                Column("age", intType)
            )
        )

        val thrown = assertFailsWith<IllegalArgumentException> { database.registerTable(withAge) }

        assertTrue("'age'" in thrown.message.orEmpty(), thrown.message.orEmpty())
        assertTrue("'id', 'name'" in thrown.message.orEmpty(), thrown.message.orEmpty())
    }
}
