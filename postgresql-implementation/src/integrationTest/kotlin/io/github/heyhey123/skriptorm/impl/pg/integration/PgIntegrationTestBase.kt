package io.github.heyhey123.skriptorm.impl.pg.integration

import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariPoolMXBean
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.impl.pg.database.PgDatabase
import io.github.heyhey123.skriptorm.impl.pg.database.PgJdbcDialect
import io.github.heyhey123.skriptorm.impl.pg.type.PgDataTypes
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.result.DataCursor
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.DataType
import io.github.heyhey123.skriptorm.type.TypeId
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.time.Duration
import java.util.UUID
import kotlin.test.assertEquals

private val IDLE_TIMEOUT: Duration = Duration.ofSeconds(5)

/**
 * Base class for integration tests that exercise the PostgreSQL implementation against a real server.
 *
 * Every test gets its own connection pool and its own lifecycle, so a test that fails mid-flight cannot
 * leave the global [Database] state behind for the next one. Tests own their tables and may drop and
 * recreate them freely.
 *
 * The columns below are declared with [PgDataTypes] rather than with the JDBC module's types, because
 * PostgreSQL is the implementation that decides what a column is stored as: a `tinyint` is a `SMALLINT`
 * and a UUID is `BYTEA` here, and a table declared with MySQL's storage names would not be created at
 * all. Reading a row back uses the same objects, since a value is converted the way its column was.
 *
 * Test methods are written as `= runBlocking<Unit> { ... }`. JUnit only discovers `@Test` methods that
 * return `void`, while helpers such as `assertFailsWith` return a value, so the explicit type argument
 * stops an expression-bodied test from silently vanishing from the run.
 */
abstract class PgIntegrationTestBase {

    protected lateinit var dataSource: HikariDataSource
        private set

    protected lateinit var database: PgDatabase
        private set

    /** Query factory created by [PgDatabase] for this test's pool. */
    protected val queries: Queries
        get() = checkNotNull(database.queries) { "The database did not initialize its queries." }

    /** Live Hikari pool metrics, used to prove that connections are returned. */
    protected val pool: HikariPoolMXBean
        get() = checkNotNull(dataSource.hikariPoolMXBean) { "The Hikari pool is not running." }

    protected val intType = pgType<Int>(TypeId.INT)
    protected val stringType = pgType<String>(TypeId.STRING)
    protected val doubleType = pgType<Double>(TypeId.DOUBLE)
    protected val floatType = pgType<Float>(TypeId.FLOAT)
    protected val bigType = pgType<Long>(TypeId.BIGINT)
    protected val tinyType = pgType<Byte>(TypeId.TINYINT)
    protected val booleanType = pgType<Boolean>(TypeId.BOOLEAN)
    protected val uuidType = pgType<UUID>(TypeId.UUID)

    /**
     * The table every example in this suite shares: an identity primary key plus one column for each
     * driver-independent type the implementation stores without Bukkit or Skript.
     */
    protected val usersTable = Table(
        "orm_users",
        listOf(
            Column("id", intType, isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
            Column("name", stringType, isNullable = false, size = 100),
            Column("age", intType),
            Column("score", doubleType),
            Column("ratio", floatType),
            Column("big", bigType),
            Column("tiny", tinyType),
            Column("active", booleanType),
            Column("uid", uuidType)
        )
    )

    @BeforeEach
    fun openDatabase() {
        val endpoint = PgTestServer.requireEndpoint()
        runBlocking {
            // A previous test class may have aborted before its teardown ran. Shutting down is not
            // conditional on there being a default connection: a named one can outlive it.
            Database.shutdown()
            Database.beginLifecycle()
            val opened = PgDatabase()
            Database.connectDefault(opened, endpoint.settings)
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
        executeSql("DROP TABLE IF EXISTS ${PgJdbcDialect.quoteIdentifier(table.name)}")
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

    /** Row count read straight from PostgreSQL, independent of the implementation's own path. */
    protected fun rawRowCount(tableName: String = usersTable.name): Long =
        queryLong("SELECT COUNT(*) FROM ${PgJdbcDialect.quoteIdentifier(tableName)}")

    /**
     * What PostgreSQL says it created, read from `information_schema` rather than from the driver, so a
     * storage name is checked as the server recorded it.
     */
    protected fun columnsOf(table: String = usersTable.name): Map<String, ColumnInfo> =
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "SELECT column_name, data_type, is_nullable, character_maximum_length, is_identity " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = current_schema() AND table_name = ?"
            ).use { statement ->
                statement.setString(1, table)
                statement.executeQuery().use { rows ->
                    buildMap {
                        while (rows.next()) {
                            put(
                                rows.getString("column_name"),
                                ColumnInfo(
                                    dataType = rows.getString("data_type"),
                                    nullable = rows.getString("is_nullable") == "YES",
                                    size = rows.getInt("character_maximum_length").takeIf { !rows.wasNull() },
                                    identity = rows.getString("is_identity") == "YES"
                                )
                            )
                        }
                    }
                }
            }
        }

    /** The primary key columns PostgreSQL reports for [table]. */
    protected fun primaryKeys(table: String = usersTable.name): List<String> =
        dataSource.connection.use { connection ->
            connection.metaData.getPrimaryKeys(connection.catalog, "public", table).use { rows ->
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

    /** What PostgreSQL recorded for one column. */
    protected data class ColumnInfo(
        val dataType: String,
        val nullable: Boolean,
        val size: Int?,
        val identity: Boolean
    )

    /** Reads the current row of this cursor as a [UserRow]. */
    protected fun DataCursor.readUser(): UserRow = UserRow(
        id = requireNotNull(get("id", intType)) { "id must not be null" },
        name = requireNotNull(get("name", stringType)) { "name must not be null" },
        age = get("age", intType),
        score = get("score", doubleType),
        ratio = get("ratio", floatType),
        big = get("big", bigType),
        tiny = get("tiny", tinyType),
        active = get("active", booleanType),
        uid = get("uid", uuidType)
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
     * Builds an insert map for [usersTable]. `id` is omitted when null so PostgreSQL generates the next
     * identity value; every other column is written explicitly, including SQL NULL.
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

    /**
     * The type this implementation stores [id] as.
     *
     * The registry holds the PostgreSQL-specific types, so a test that declared a column with anything
     * else would be testing a table this implementation never creates.
     */
    private fun <T : Any> pgType(id: TypeId): DataType<T> {
        val type = requireNotNull(PgDataTypes.typesRegistry[id]) {
            "The PostgreSQL implementation declares no type for $id."
        }
        @Suppress("UNCHECKED_CAST")
        return type as DataType<T>
    }
}
