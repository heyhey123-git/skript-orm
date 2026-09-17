package io.github.heyhey123.xiaojieorm.impl.jdbc.integration

import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariPoolMXBean
import io.github.heyhey123.xiaojieorm.database.Database
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.JdbcDatabase
import io.github.heyhey123.xiaojieorm.impl.jdbc.database.MysqlJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.BigIntJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.BooleanJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.DoubleJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.FloatJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.TinyIntJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.UuidJdbcDataType
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.result.DataCursor
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.DataType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.time.Duration
import java.util.UUID
import kotlin.test.assertEquals

private val IDLE_TIMEOUT: Duration = Duration.ofSeconds(5)

/**
 * Base class for integration tests that exercise the JDBC implementation against a real MySQL
 * server.
 *
 * Every test gets its own connection pool and its own lifecycle, so a test that fails mid-flight
 * cannot leave the global [Database] state behind for the next one. Tests own their tables and may
 * drop and recreate them freely.
 *
 * Test methods are written as `= runBlocking<Unit> { ... }`. JUnit only discovers `@Test` methods
 * that return `void`, while helpers such as `assertFailsWith` and `assertNotNull` return a value, so
 * the explicit type argument stops an expression-bodied test from silently vanishing from the run.
 */
abstract class MysqlIntegrationTestBase {

    protected lateinit var dataSource: HikariDataSource
        private set

    protected lateinit var database: JdbcDatabase
        private set

    /** Query factory created by [JdbcDatabase.doConnect] for this test's pool. */
    protected val queries: Queries
        get() = checkNotNull(database.queries) { "The database did not initialize its queries." }

    /** Live Hikari pool metrics, used to prove that connections are returned. */
    protected val pool: HikariPoolMXBean
        get() = checkNotNull(dataSource.hikariPoolMXBean) { "The Hikari pool is not running." }

    /**
     * The table every example in this suite shares: an auto-increment primary key plus one column
     * for each driver-independent JDBC type the implementation stores without Bukkit or Skript.
     */
    protected val usersTable = Table(
        "users",
        listOf(
            Column("id", IntJdbcDataType(), isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
            Column("name", StringJdbcDataType(), isNullable = false, size = 100),
            Column("age", IntJdbcDataType()),
            Column("score", DoubleJdbcDataType()),
            Column("ratio", FloatJdbcDataType()),
            Column("big", BigIntJdbcDataType()),
            Column("tiny", TinyIntJdbcDataType()),
            Column("active", BooleanJdbcDataType()),
            Column("uid", UuidJdbcDataType())
        )
    )

    @BeforeEach
    fun openDatabase() {
        val endpoint = MysqlTestServer.requireEndpoint()
        runBlocking {
            // A previous test class may have aborted before its teardown ran. Shutting down is not
            // conditional on there being a default connection: a named one can outlive it.
            Database.shutdown()
            Database.beginLifecycle()
            val opened = JdbcDatabase(endpoint.driverClassName, MysqlJdbcDialect)
            Database.replaceWith(opened, endpoint.jdbcUrl, endpoint.username, endpoint.password)
            database = opened
        }
        dataSource = checkNotNull(database.dataSource) { "The JDBC database did not create a data source." }
    }

    @AfterEach
    fun closeDatabase() {
        runBlocking { Database.shutdown() }
    }

    /** Drops [table] and registers it again, so a test starts from an empty, fully typed table. */
    protected suspend fun recreateTable(table: Table = usersTable) {
        executeSql("DROP TABLE IF EXISTS ${MysqlJdbcDialect.quoteIdentifier(table.name)}")
        database.registerTable(table)
    }

    /** Runs a statement outside the implementation, to prepare or verify state independently. */
    protected fun executeSql(sql: String) {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement -> statement.execute(sql) }
        }
    }

    /** Reads a single-column value outside the implementation. */
    protected fun queryLong(sql: String): Long =
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(sql).use { rows ->
                    check(rows.next()) { "Expected the query to return one row: $sql" }
                    rows.getLong(1)
                }
            }
        }

    /** Row count read straight from MySQL, independent of the implementation's own path. */
    protected fun rawRowCount(tableName: String = usersTable.name): Long =
        queryLong("SELECT COUNT(*) FROM ${MysqlJdbcDialect.quoteIdentifier(tableName)}")

    /** The columns MySQL reports for [table]. */
    protected fun columnMetadata(table: String): List<ColumnMeta> =
        dataSource.connection.use { connection ->
            connection.metaData.getColumns(connection.catalog, null, table, null).use { rows ->
                buildList {
                    while (rows.next()) {
                        add(
                            ColumnMeta(
                                name = rows.getString("COLUMN_NAME"),
                                typeName = rows.getString("TYPE_NAME"),
                                size = rows.getInt("COLUMN_SIZE"),
                                nullable = rows.getString("IS_NULLABLE") == "YES"
                            )
                        )
                    }
                }
            }
        }

    /** The primary key columns MySQL reports for [table]. */
    protected fun primaryKeys(table: String): List<String> =
        dataSource.connection.use { connection ->
            connection.metaData.getPrimaryKeys(connection.catalog, null, table).use { rows ->
                buildList { while (rows.next()) add(rows.getString("COLUMN_NAME")) }
            }
        }

    /** Every row of [usersTable], ordered so assertions do not depend on storage order. */
    protected suspend fun allUsers(): List<UserRow> =
        queries.selectMany(null).execute(usersTable).readUsers().sortedBy { it.id }

    /**
     * Waits until the pool reports [expected] checked-out connections. Hikari returns a connection
     * synchronously, but a short wait keeps the leak assertions robust on a loaded machine.
     */
    protected fun awaitIdleConnections(expected: Int = 0) {
        val deadline = System.nanoTime() + IDLE_TIMEOUT.toNanos()
        while (System.nanoTime() < deadline) {
            if (pool.activeConnections == expected) return
            Thread.sleep(10)
        }
        assertEquals(expected, pool.activeConnections, "The connection pool did not settle.")
    }

    /** One row of [usersTable], typed the same way the table is declared. */
    protected data class UserRow(
        val id: Int,
        val name: String,
        val age: Int?,
        val score: Double?,
        val ratio: Float?,
        val big: Long?,
        val tiny: Byte?,
        val active: Boolean?,
        val uid: UUID?
    )

    /** What MySQL reports for one column. */
    protected data class ColumnMeta(
        val name: String,
        val typeName: String,
        val size: Int,
        val nullable: Boolean
    )

    /** Reads the current row of this cursor as a [UserRow]. */
    protected fun DataCursor.readUser(): UserRow = UserRow(
        id = requireNotNull(get("id", IntJdbcDataType())) { "id must not be null" },
        name = requireNotNull(get("name", StringJdbcDataType())) { "name must not be null" },
        age = get("age", IntJdbcDataType()),
        score = get("score", DoubleJdbcDataType()),
        ratio = get("ratio", FloatJdbcDataType()),
        big = get("big", BigIntJdbcDataType()),
        tiny = get("tiny", TinyIntJdbcDataType()),
        active = get("active", BooleanJdbcDataType()),
        uid = get("uid", UuidJdbcDataType())
    )

    /** Drains this result into rows and always closes the cursor. */
    protected fun CursorResult.readUsers(): List<UserRow> = cursor.use { open ->
        buildList {
            while (open.next()) add(open.readUser())
        }
    }

    /** Reads one column of every row of this result, closing the cursor. */
    protected fun <T : Any> CursorResult.readColumn(column: String, type: DataType<T>): List<T?> =
        cursor.use { open ->
            buildList { while (open.next()) add(open.get(column, type)) }
        }

    /**
     * Builds an insert map for [usersTable]. `id` is omitted when null so MySQL assigns the next
     * auto-increment value; every other column is written explicitly, including SQL NULL.
     */
    protected fun userValues(
        name: String,
        id: Int? = null,
        age: Int? = null,
        score: Double? = null,
        ratio: Float? = null,
        big: Long? = null,
        tiny: Byte? = null,
        active: Boolean? = null,
        uid: UUID? = null
    ): Map<String, Any?> = linkedMapOf<String, Any?>().apply {
        id?.let { put("id", it) }
        put("name", name)
        put("age", age)
        put("score", score)
        put("ratio", ratio)
        put("big", big)
        put("tiny", tiny)
        put("active", active)
        put("uid", uid)
    }
}
