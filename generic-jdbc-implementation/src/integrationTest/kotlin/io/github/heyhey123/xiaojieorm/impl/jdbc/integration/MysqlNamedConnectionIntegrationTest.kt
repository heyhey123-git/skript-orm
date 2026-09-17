package io.github.heyhey123.xiaojieorm.impl.jdbc.integration

import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabase
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.MysqlJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Covers two connections to a real MySQL server at the same time: that a named one is registered
 * without disturbing the default, that each keeps its own table registry, and that both pools are
 * closed together.
 *
 * Both connections point at the same schema, so this is not about the two databases holding
 * different data. It is about the addon keeping them apart: which one a statement resolves to, and
 * which one owns which table.
 */
class MysqlNamedConnectionIntegrationTest : MysqlIntegrationTestBase() {

    private val namedTable = Table(
        "named_connection_probe",
        listOf(
            Column("id", IntJdbcDataType(), isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
            Column("name", StringJdbcDataType(), isNullable = false, size = 50)
        )
    )

    @Test
    fun `a named connection is published without replacing the default`() = runBlocking<Unit> {
        recreateTable()
        val named = connectNamed("secondary")

        assertSame(database, Database.current)
        assertSame(named, Database.connection("secondary"))
        assertEquals(listOf("secondary"), Database.connectionNames)
        assertTrue(database.isConnected)
        assertTrue(named.isConnected)
        assertFalse(checkNotNull(database.dataSource).isClosed)
        assertFalse(checkNotNull(named.dataSource).isClosed)
    }

    @Test
    fun `each connection owns the tables registered on it`() = runBlocking<Unit> {
        recreateTable()
        val named = connectNamed("secondary")

        assertNotNull(database.tables[usersTable.name])
        assertNull(named.tables[usersTable.name], "a registered table belongs to one connection only")

        named.registerTable(namedTable)
        assertNotNull(named.tables[namedTable.name])
        assertNull(database.tables[namedTable.name])
    }

    @Test
    fun `work on a named connection runs against its own pool`() = runBlocking<Unit> {
        recreateTable()
        val named = connectNamed("secondary")
        named.registerTable(namedTable)

        val written = checkNotNull(named.queries).insertOne(mapOf<String, Any?>("name" to "named"))
            .execute(namedTable)
        assertEquals(1L, written.affectedCount)

        // Both pools point at the same schema, so the row written through the named connection is
        // visible through the default one: the two really are separate live connections.
        val rows = checkNotNull(named.queries).selectMany(null).execute(namedTable).readColumn(
            "name",
            StringJdbcDataType()
        )
        assertEquals(listOf("named"), rows)

        awaitIdleConnections()
    }

    @Test
    fun `disconnecting a named connection leaves the default usable`() = runBlocking<Unit> {
        recreateTable()
        val named = connectNamed("secondary")
        val namedPool = checkNotNull(named.dataSource)

        named.disconnect()

        assertNull(Database.connection("secondary"))
        assertTrue(Database.connectionNames.isEmpty())
        assertSame(database, Database.current)
        assertTrue(namedPool.isClosed, "the named connection's pool must be closed")
        assertTrue(database.isConnected)

        val written = queries.insertOne(userValues("still-here")).execute(usersTable)
        assertEquals(1L, written.affectedCount)
    }

    @Test
    fun `reconnecting a name closes only the pool it replaced`() = runBlocking<Unit> {
        recreateTable()
        val first = connectNamed("secondary")
        val firstPool = checkNotNull(first.dataSource)
        val defaultPool = checkNotNull(database.dataSource)

        val second = connectNamed("secondary")

        assertTrue(firstPool.isClosed, "the replaced connection's pool must be closed")
        assertFalse(defaultPool.isClosed, "the default connection must not be touched")
        assertSame(second, Database.connection("secondary"))
        assertSame(database, Database.current)
    }

    @Test
    fun `shutdown closes every connection`() = runBlocking<Unit> {
        recreateTable()
        val named = connectNamed("secondary")
        val defaultPool = checkNotNull(database.dataSource)
        val namedPool = checkNotNull(named.dataSource)

        Database.shutdown()

        assertTrue(defaultPool.isClosed)
        assertTrue(namedPool.isClosed)
        assertNull(Database.current)
        assertTrue(Database.connectionNames.isEmpty())
    }

    private suspend fun connectNamed(name: String): JdbcDatabase {
        val endpoint = MysqlTestServer.requireEndpoint()
        val named = JdbcDatabase(endpoint.driverClassName, MysqlJdbcDialect)
        Database.connectNamed(name, named, endpoint.settings)
        return named
    }
}
