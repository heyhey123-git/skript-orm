package io.github.heyhey123.skriptorm.impl.jdbc.integration

import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.database.DatabaseRegistry
import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDatabase
import io.github.heyhey123.skriptorm.impl.jdbc.database.MariadbDatabaseFactory
import io.github.heyhey123.skriptorm.impl.jdbc.database.MysqlJdbcDialect
import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Covers the `"MariaDB"` connection type against a real MariaDB server: the factory that builds it,
 * and the driver it names.
 *
 * The URL check is the one an author meets first. The MariaDB connector and Connector/J both speak the
 * MySQL protocol but each claims only its own URL scheme, so a connection declared as `"MariaDB"` and
 * pointed at a `jdbc:mysql://` URL has to fail at connect time instead of quietly running on the
 * other driver: selecting the driver by URL is exactly what a server cannot do, because it has to
 * know which driver to load before it can ask whether that driver accepts the URL.
 */
class MariadbFactoryIntegrationTest {

    private lateinit var endpoint: MysqlTestServer.Endpoint

    /** Registered through the real DDL path, under a name unique to this class. */
    private val probeTable = Table(
        "mariadb_probe",
        listOf(
            Column("id", IntJdbcDataType(), isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
            Column("name", StringJdbcDataType(), isNullable = false, size = 50)
        )
    )

    @BeforeEach
    fun bindServer() {
        endpoint = MysqlTestServer.of(MysqlTestServer.Product.MARIADB).requireEndpoint()
        resetLifecycle()
    }

    @AfterEach
    fun releaseLifecycle() {
        resetLifecycle()
    }

    @Test
    fun `the mariadb factory resolves the driver and selects the mysql family dialect`() {
        assertEquals("MariaDB", MariadbDatabaseFactory.typeName)
        assertEquals("org.mariadb.jdbc.Driver", MariadbDatabaseFactory.driverName)

        val database = MariadbDatabaseFactory.create(emptyMap())

        assertEquals("org.mariadb.jdbc.Driver", database.driver)
        // MySQL and MariaDB send the same SQL, so the type shares the dialect rather than carrying a
        // copy of it that would have to be kept in step.
        assertSame(MysqlJdbcDialect, database.dialect)
    }

    @Test
    fun `the registry resolves the mariadb factory once it has been loaded`() {
        // The factory registers itself from its init block, which runs the first time anything touches
        // the object. Reading it here keeps this test independent of which test ran first.
        val factory = MariadbDatabaseFactory
        assertEquals("MariaDB", factory.typeName)

        val database = assertIs<JdbcDatabase>(DatabaseRegistry.get("MariaDB", emptyMap()))

        assertEquals("org.mariadb.jdbc.Driver", database.driver)
        assertSame(MysqlJdbcDialect, database.dialect)
    }

    @Test
    fun `the mariadb factory can connect and register a table`() = runBlocking<Unit> {
        val database = MariadbDatabaseFactory.create(emptyMap())

        Database.connectDefault(database, endpoint.settings)
        database.registerTable(probeTable)

        val written = database.queries!!.insertOne(mapOf<String, Any?>("name" to "factory"))
            .execute(probeTable)
        assertEquals(1L, written.affectedCount)
    }

    @Test
    fun `a mysql url is refused instead of being run on the mariadb connector`() {
        val database = JdbcDatabase(MariadbDatabaseFactory.driverName, MysqlJdbcDialect)
        val mysqlUrl = endpoint.jdbcUrl.replaceFirst("jdbc:mariadb:", "jdbc:mysql:")
        assertTrue(mysqlUrl != endpoint.jdbcUrl, "the MariaDB url is not a jdbc:mariadb: url: ${endpoint.jdbcUrl}")

        val failure = assertFailsWith<IllegalStateException> {
            runBlocking { Database.connectDefault(database, endpoint.settings.copy(url = mysqlUrl)) }
        }

        // The driver's own refusal is what a reader needs to see, so it is reported rather than
        // restated: it names the driver and the URL it was given.
        val reason = generateSequence<Throwable>(failure) { it.cause }
            .mapNotNull { it.message }
            .joinToString("\n")
        assertTrue("claims to not accept jdbcUrl" in reason, reason)
        assertNull(Database.current, "a refused connection must not be published")
        assertNull(database.dataSource, "a refused connection must not keep a pool")
    }

    /** Leaves the global lifecycle with no connection registered and open for the next test. */
    private fun resetLifecycle() {
        runBlocking {
            // Unconditional: a named connection can outlive the default one, so a null `current` does
            // not mean nothing is registered.
            Database.shutdown()
            Database.beginLifecycle()
        }
    }
}
