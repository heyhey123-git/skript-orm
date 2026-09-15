package io.github.heyhey123.xiaojieorm.impl.jdbc.integration

import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.database.DatabaseRegistry
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.GenericJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabase
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabaseFactory
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.MysqlDatabaseFactory
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.MysqlJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Covers the global [Database] lifecycle against a real server: connecting, publishing, failing,
 * replacing, disconnecting and shutting down, plus the factories that build a JDBC database.
 *
 * The lifecycle is process-global, so every test starts and ends with no current database. Test
 * methods use `runBlocking<Unit>` for the reason explained in [MysqlIntegrationTestBase].
 */
class MysqlDatabaseLifecycleIntegrationTest {

    private lateinit var endpoint: MysqlTestServer.Endpoint

    /**
     * Registered through the real DDL path. The name is unique to this class so that the tests do
     * not depend on, or interfere with, the tables other integration tests create.
     */
    private val probeTable = Table(
        "lifecycle_probe",
        listOf(
            Column("id", IntJdbcDataType(), isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
            Column("name", StringJdbcDataType(), isNullable = false, size = 50)
        )
    )

    @BeforeEach
    fun bindServer() {
        endpoint = MysqlTestServer.requireEndpoint()
        resetLifecycle()
    }

    @AfterEach
    fun releaseLifecycle() {
        resetLifecycle()
    }

    @Test
    fun `replaceWith connects and publishes the database`() = runBlocking<Unit> {
        val database = JdbcDatabase(endpoint.driverClassName, MysqlJdbcDialect)

        Database.replaceWith(database, endpoint.jdbcUrl, endpoint.username, endpoint.password)

        assertTrue(database.isConnected)
        assertEquals(Database.State.CONNECTED, database.state)
        assertSame(database, Database.current)
        assertNotNull(database.queries)
        assertNotNull(database.dataSource)
    }

    @Test
    fun `registering a table before connecting is rejected`() = runBlocking<Unit> {
        val database = JdbcDatabase(endpoint.driverClassName, MysqlJdbcDialect)

        assertFailsWith<IllegalStateException> { database.registerTable(probeTable) }
    }

    @Test
    fun `a rejected password leaves no database behind`() = runBlocking<Unit> {
        val database = JdbcDatabase(endpoint.driverClassName, MysqlJdbcDialect)

        assertFailsWith<IllegalStateException> {
            Database.replaceWith(database, endpoint.jdbcUrl, endpoint.username, "definitely-not-the-password")
        }

        assertNull(Database.current)
        assertEquals(Database.State.DISCONNECTED, database.state)
        assertNull(database.dataSource)
        assertNull(database.queries)
    }

    @Test
    fun `an unreachable server fails the connection attempt`() = runBlocking<Unit> {
        val database = JdbcDatabase(endpoint.driverClassName, MysqlJdbcDialect)
        val unreachable = "jdbc:mysql://127.0.0.1:1/xiaojie_orm_test?connectTimeout=1000&socketTimeout=1000"

        assertFailsWith<IllegalStateException> {
            Database.replaceWith(database, unreachable, endpoint.username, endpoint.password)
        }

        assertNull(Database.current)
        assertNull(database.dataSource)
    }

    @Test
    fun `a database can be disconnected and connected again`() = runBlocking<Unit> {
        val database = JdbcDatabase(endpoint.driverClassName, MysqlJdbcDialect)
        Database.replaceWith(database, endpoint.jdbcUrl, endpoint.username, endpoint.password)
        database.registerTable(probeTable)

        database.disconnect()

        assertFalse(database.isConnected)
        assertEquals(Database.State.DISCONNECTED, database.state)
        assertNull(Database.current)

        Database.replaceWith(database, endpoint.jdbcUrl, endpoint.username, endpoint.password)

        assertTrue(database.isConnected)
        // Tables registered before the disconnect are registered again during the reconnect.
        val written = database.queries!!.insertOne(mapOf<String, Any?>("name" to "after-reconnect"))
            .execute(probeTable)
        assertEquals(1L, written.affectedCount)
    }

    @Test
    fun `shutdown closes the pool and blocks further work`() = runBlocking<Unit> {
        val database = JdbcDatabase(endpoint.driverClassName, MysqlJdbcDialect)
        Database.replaceWith(database, endpoint.jdbcUrl, endpoint.username, endpoint.password)
        val pool = checkNotNull(database.dataSource)

        Database.shutdown()

        assertNull(Database.current)
        assertFalse(database.isConnected)
        assertNull(database.dataSource)
        assertTrue(pool.isClosed, "shutdown must close the connection pool")
        assertFailsWith<IllegalStateException> { database.withQueries { } }
    }

    @Test
    fun `replacing the current database closes the previous pool`() = runBlocking<Unit> {
        val first = JdbcDatabase(endpoint.driverClassName, MysqlJdbcDialect)
        Database.replaceWith(first, endpoint.jdbcUrl, endpoint.username, endpoint.password)
        val firstPool = checkNotNull(first.dataSource)

        val second = JdbcDatabase(endpoint.driverClassName, MysqlJdbcDialect)
        Database.replaceWith(second, endpoint.jdbcUrl, endpoint.username, endpoint.password)

        assertTrue(firstPool.isClosed, "the replaced pool must be closed")
        assertFalse(first.isConnected)
        assertSame(second, Database.current)
        assertTrue(second.isConnected)
    }

    @Test
    fun `mysql factory resolves the driver and selects the mysql dialect`() {
        assertEquals("MySQL", MysqlDatabaseFactory.typeName)
        assertEquals("com.mysql.cj.jdbc.Driver", MysqlDatabaseFactory.driverName)

        val database = MysqlDatabaseFactory.create(emptyMap())

        assertEquals("com.mysql.cj.jdbc.Driver", database.driver)
        assertSame(MysqlJdbcDialect, database.dialect)
    }

    @Test
    fun `the registry resolves the mysql factory by type name`() {
        val database = assertIs<JdbcDatabase>(DatabaseRegistry.get("MySQL", emptyMap()))

        assertEquals("com.mysql.cj.jdbc.Driver", database.driver)
        assertSame(MysqlJdbcDialect, database.dialect)
    }

    @Test
    fun `the generic jdbc factory still requires a driver property`() {
        assertFailsWith<IllegalArgumentException> { JdbcDatabaseFactory.create(emptyMap()) }

        val database = JdbcDatabaseFactory.create(mapOf("driver" to "com.mysql.cj.jdbc.Driver"))

        assertSame(GenericJdbcDialect, database.dialect)
    }

    @Test
    fun `the mysql factory can connect and register a table`() = runBlocking<Unit> {
        val database = MysqlDatabaseFactory.create(emptyMap())

        Database.replaceWith(database, endpoint.jdbcUrl, endpoint.username, endpoint.password)
        database.registerTable(probeTable)

        val written = database.queries!!.insertOne(mapOf<String, Any?>("name" to "factory"))
            .execute(probeTable)
        assertEquals(1L, written.affectedCount)
    }

    /** Leaves the global lifecycle with no current database and open for the next test. */
    private fun resetLifecycle() {
        runBlocking {
            if (Database.current != null) Database.shutdown()
            Database.beginLifecycle()
        }
    }
}
