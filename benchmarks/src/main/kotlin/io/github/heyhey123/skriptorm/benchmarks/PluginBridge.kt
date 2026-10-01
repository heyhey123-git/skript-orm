package io.github.heyhey123.skriptorm.benchmarks

import io.github.heyhey123.skriptorm.database.ConnectionSettings
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.database.DatabaseRegistry
import io.github.heyhey123.skriptorm.impl.jdbc.database.GenericJdbcDialect
import io.github.heyhey123.skriptorm.impl.jdbc.database.JdbcDatabaseFactory
import io.github.heyhey123.skriptorm.impl.jdbc.type.BigIntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.BooleanJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.DoubleJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.skriptorm.impl.jdbc.type.StringJdbcDataType
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.result.WriteResult
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.DriverManager

/**
 * The plugin's own insert path, reachable from a Java benchmark.
 *
 * JMH's generator reads Java sources, so the cases are Java, and a Java class cannot call a Kotlin
 * `suspend` function without building a continuation by hand. Every function here therefore does its
 * own `runBlocking` and answers a plain value. Nothing else is bridged: the table is built where the
 * column types live, and the raw SQL that clears and counts the table sits outside the plugin on
 * purpose, so the numbers it produces are not the plugin's own report of itself.
 *
 * What this drives is what a script drives: register the factory through the registry, let the plugin
 * build its own pool from a url and a driver class name, register a table, and insert through the
 * plugin's query objects.
 */
object PluginBridge {

    /** H2's in-memory form needs no credentials, and the plugin requires a username to be present. */
    private const val USERNAME = "sa"

    private const val PASSWORD = ""

    private var database: Database? = null

    /** The url without the counting driver in front of it, for statements that must not be counted. */
    private var plainUrl: String? = null

    private val queries: Queries
        get() = checkNotNull(database?.queries) { "The plugin is not connected: connect() first." }

    /**
     * Opens a database the way a script's `database` statement does, but through the counting driver.
     *
     * @param countingUrl the url as the plugin sees it, `jdbc:counting:` in front of [plainUrl].
     * @param driver the class the plugin's `driver` property names.
     * @param plainUrl the same url without the counting driver, for this bridge's own statements.
     */
    @JvmStatic
    fun connect(countingUrl: String, driver: String, plainUrl: String) {
        this.plainUrl = plainUrl
        runBlocking {
            // A lifecycle left over from an earlier trial in the same JVM would refuse the second one.
            Database.shutdown()
            Database.beginLifecycle()
            // Touching the factory is what registers it: the registry is filled by the object's init.
            val opened = DatabaseRegistry.get(
                JdbcDatabaseFactory.typeName,
                mapOf(JdbcDatabaseFactory.DRIVER_PROPERTY to driver)
            )
            Database.connectDefault(opened, ConnectionSettings(countingUrl, USERNAME, PASSWORD))
            database = opened
        }
    }

    @JvmStatic
    fun disconnect() {
        runBlocking { Database.shutdown() }
        database = null
        plainUrl = null
    }

    /**
     * The shape the user's own MySQL benchmark used: an auto-increment primary key and five ordinary
     * columns, which is six columns and therefore exactly the 30 000 values this plugin puts in one
     * statement before it splits.
     *
     * Registering this against the generic dialect is expected to fail, because only the MySQL dialect
     * answers `autoIncrementClause()` — see [suppliedIdTable] for the shape that runs. It is built
     * anyway so the refusal is measured rather than assumed.
     */
    @JvmStatic
    fun declaredTable(): Table = Table(
        "bench_declared",
        listOf(
            Column("id", BigIntJdbcDataType(), isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
            Column("name", StringJdbcDataType(), isNullable = false, size = 64),
            Column("age", IntJdbcDataType(), isNullable = true),
            Column("score", DoubleJdbcDataType(), isNullable = false),
            Column("active", BooleanJdbcDataType(), isNullable = false),
            Column("big", BigIntJdbcDataType(), isNullable = true)
        )
    )

    /** The same six columns with the key supplied by the caller, which every dialect can render. */
    @JvmStatic
    fun suppliedIdTable(): Table = Table(
        "bench_supplied",
        listOf(
            Column("id", BigIntJdbcDataType(), isPrimaryKey = true, isNullable = false),
            Column("name", StringJdbcDataType(), isNullable = false, size = 64),
            Column("age", IntJdbcDataType(), isNullable = true),
            Column("score", DoubleJdbcDataType(), isNullable = false),
            Column("active", BooleanJdbcDataType(), isNullable = false),
            Column("big", BigIntJdbcDataType(), isNullable = true)
        )
    )

    @JvmStatic
    fun registerTable(table: Table) {
        val open = checkNotNull(database) { "The plugin is not connected: connect() first." }
        runBlocking { open.registerTable(table) }
    }

    /**
     * Rows keyed by column name, which is the shape `insert many` takes. Ids are supplied and constant,
     * so the same rows can be inserted again once the table has been cleared.
     */
    @JvmStatic
    fun rows(count: Int): List<Map<String, Any?>> = (0 until count).map { index ->
        mapOf(
            "id" to index.toLong(),
            "name" to "name-$index",
            "age" to index,
            "score" to index * 1.5,
            "active" to (index % 2 == 0),
            "big" to index.toLong() * 1000L
        )
    }

    /** The measured call: one `insert many`, answered with the plugin's own result. */
    @JvmStatic
    fun insertMany(table: Table, rows: List<Map<String, Any?>>): WriteResult =
        runBlocking { queries.insertMany(rows).execute(table) }

    /** Deletes every row outside the plugin, so the table does not grow across iterations. */
    @JvmStatic
    fun clear(table: Table): Long = plain { connection ->
        connection.createStatement().use { statement ->
            statement.executeUpdate("DELETE FROM ${GenericJdbcDialect.renderIdentifier(table.name)}").toLong()
        }
    }

    /** Counts the rows outside the plugin: the driver's word for what it did is not evidence of it. */
    @JvmStatic
    fun count(table: Table): Long = plain { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM ${GenericJdbcDialect.renderIdentifier(table.name)}").use { rows ->
                if (rows.next()) rows.getLong(1) else 0L
            }
        }
    }

    private fun <T> plain(block: (Connection) -> T): T {
        val url = checkNotNull(plainUrl) { "The plugin is not connected: connect() first." }
        return DriverManager.getConnection(url, USERNAME, PASSWORD).use(block)
    }
}
