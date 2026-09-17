package io.github.heyhey123.xiaojieorm.database

import io.github.heyhey123.xiaojieorm.condition.WhereClause
import io.github.heyhey123.xiaojieorm.queries.Delete
import io.github.heyhey123.xiaojieorm.queries.DeleteById
import io.github.heyhey123.xiaojieorm.queries.InsertIfAbsent
import io.github.heyhey123.xiaojieorm.queries.InsertMany
import io.github.heyhey123.xiaojieorm.queries.InsertOne
import io.github.heyhey123.xiaojieorm.queries.Queries
import io.github.heyhey123.xiaojieorm.queries.SelectById
import io.github.heyhey123.xiaojieorm.queries.SelectMany
import io.github.heyhey123.xiaojieorm.queries.SelectOne
import io.github.heyhey123.xiaojieorm.queries.SelectPage
import io.github.heyhey123.xiaojieorm.queries.Update
import io.github.heyhey123.xiaojieorm.queries.UpdateById
import io.github.heyhey123.xiaojieorm.queries.UpsertById
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.DataTypes
import io.github.heyhey123.xiaojieorm.type.IntDataType
import io.github.heyhey123.xiaojieorm.type.TypeId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private const val GATE_TIMEOUT_SECONDS = 10L

private val URL = "jdbc:test:lifecycle"
private val USER = "tester"
private val PASSWORD = "secret"
private val SETTINGS = ConnectionSettings(URL, USER, PASSWORD)

/**
 * Covers the process-global [Database] lifecycle: publishing, replacing, failing, disconnecting,
 * shutting down, registering tables, and the concurrency guarantees of all of those.
 *
 * The lifecycle belongs to a companion object, so every test starts and ends with no current
 * database. A controllable implementation makes the transitions observable: it can hold a
 * connection or a disconnect open, and it records what the lifecycle asked it to do.
 *
 * Test methods are written as `= runBlocking<Unit> { ... }`. JUnit only discovers `@Test` methods
 * that return `void`, and helpers such as `assertFailsWith` return a value, so an expression-bodied
 * test would otherwise be compiled, never run, and never reported.
 */
class DatabaseLifecycleTest {

    @BeforeEach
    fun prepareLifecycle() = resetLifecycle()

    @AfterEach
    fun releaseLifecycle() = resetLifecycle()

    // ---------------------------------------------------------------- initial state

    @Test
    fun `a new database starts disconnected and unpublished`() {
        val database = ControllableDatabase("fresh")

        assertFalse(database.isConnected)
        assertEquals(Database.State.DISCONNECTED, database.state)
        assertNull(database.queries)
        assertTrue(database.tables.isEmpty())
        assertNull(Database.current)
        assertFalse(Database.isShuttingDown)
    }

    @Test
    fun `connect is not attempted until connectDefault runs`() = runBlocking<Unit> {
        val database = ControllableDatabase("idle")

        assertEquals(0, database.connectCalls.get())
        assertEquals(0, database.disconnectCalls.get())
    }

    // ---------------------------------------------------------------- connecting

    @Test
    fun `connectDefault connects and publishes the database`() = runBlocking<Unit> {
        val database = ControllableDatabase("first")

        Database.connectDefault(database, SETTINGS)

        assertTrue(database.isConnected)
        assertEquals(Database.State.CONNECTED, database.state)
        assertSame(database, Database.current)
        assertSame(UnusedQueries, database.queries)
        assertEquals(1, database.connectCalls.get())
    }

    @Test
    fun `connectDefault disconnects the database it replaces`() = runBlocking<Unit> {
        val first = connectedDatabase("first")
        val second = ControllableDatabase("second")

        Database.connectDefault(second, SETTINGS)

        assertEquals(1, first.disconnectCalls.get())
        assertFalse(first.isConnected)
        assertNull(first.queries)
        assertSame(second, Database.current)
        assertTrue(second.isConnected)
    }

    @Test
    fun `a failed connect publishes nothing`() = runBlocking<Unit> {
        val database = ControllableDatabase("failing")
        database.connectFailure = IllegalStateException("connect refused")

        val thrown = assertFailsWith<IllegalStateException> {
            Database.connectDefault(database, SETTINGS)
        }

        assertEquals("connect refused", thrown.message)
        assertNull(Database.current)
        assertEquals(Database.State.DISCONNECTED, database.state)
        assertNull(database.queries)
        assertEquals(1, database.disconnectCalls.get(), "a failed connect must clean up after itself")
    }

    @Test
    fun `a failed connect keeps its cleanup failure suppressed`() = runBlocking<Unit> {
        val database = ControllableDatabase("failing")
        database.connectFailure = IllegalStateException("connect refused")
        database.disconnectFailure = IllegalArgumentException("cleanup refused")

        val thrown = assertFailsWith<IllegalStateException> {
            Database.connectDefault(database, SETTINGS)
        }

        assertEquals("connect refused", thrown.message)
        assertEquals(
            listOf("cleanup refused"),
            thrown.suppressedExceptions.map { it.message },
            "the cleanup failure must not replace the failure that caused it"
        )
    }

    @Test
    fun `an implementation that publishes no queries fails the connect`() = runBlocking<Unit> {
        val database = ControllableDatabase("no-queries")
        database.publishQueries = false

        val thrown = assertFailsWith<IllegalStateException> {
            Database.connectDefault(database, SETTINGS)
        }

        assertTrue(
            thrown.message.orEmpty().contains("did not initialize queries"),
            "unexpected message: ${thrown.message}"
        )
        assertNull(Database.current)
        assertEquals(Database.State.DISCONNECTED, database.state)
        assertEquals(1, database.disconnectCalls.get())
    }

    // ---------------------------------------------------------------- table registration

    @Test
    fun `registering a table publishes it only after it succeeds`() = runBlocking<Unit> {
        val database = connectedDatabase()
        val table = table("users")

        database.registerTable(table)

        assertEquals(listOf("users"), database.registeredTables)
        assertSame(table, database.tables["users"])
    }

    @Test
    fun `registering the same name again replaces the published definition`() = runBlocking<Unit> {
        val database = connectedDatabase()
        database.registerTable(table("users", "id"))
        val replacement = table("users", "id", "nickname")

        database.registerTable(replacement)

        assertSame(replacement, database.tables["users"])
        assertEquals(listOf("users", "users"), database.registeredTables)
    }

    @Test
    fun `a failed registration leaves the registry untouched`() = runBlocking<Unit> {
        val database = connectedDatabase()
        val published = table("users", "id")
        database.registerTable(published)
        database.registerFailure = IllegalArgumentException("ddl rejected")

        assertFailsWith<IllegalArgumentException> { database.registerTable(table("users", "id", "nickname")) }

        assertSame(
            published,
            database.tables["users"],
            "a failed registration must not overwrite the definition that did register"
        )
    }

    @Test
    fun `a failed registration does not publish a new table`() = runBlocking<Unit> {
        val database = connectedDatabase()
        database.registerFailure = IllegalArgumentException("ddl rejected")

        assertFailsWith<IllegalArgumentException> { database.registerTable(table("orders")) }

        assertTrue(database.tables.isEmpty(), "a table must not be published unless registration succeeded")
    }

    @Test
    fun `registering while disconnected is rejected`() = runBlocking<Unit> {
        val database = ControllableDatabase("disconnected")

        assertFailsWith<IllegalStateException> { database.registerTable(table("users")) }

        assertTrue(database.tables.isEmpty())
        assertEquals(0, database.disconnectCalls.get())
    }

    @Test
    fun `tables registered before a reconnect are registered again`() = runBlocking<Unit> {
        val database = connectedDatabase()
        database.registerTable(table("users"))
        database.disconnect()
        database.registeredTables.clear()

        Database.connectDefault(database, SETTINGS)

        assertEquals(listOf("users"), database.registeredTables)
    }

    // ---------------------------------------------------------------- operation leases

    @Test
    fun `disconnect waits for in-flight operations`() = runBlocking<Unit> {
        val database = connectedDatabase()
        val operationGate = CountDownLatch(1)
        val operationStarted = CountDownLatch(1)

        val operation = async(Dispatchers.IO) {
            database.withQueries { _ ->
                operationStarted.countDown()
                operationGate.awaitOpened()
            }
        }
        operationStarted.awaitOpened()

        val disconnecting = async(Dispatchers.IO) { database.disconnect() }
        awaitUntil("the database enters DISCONNECTING") { database.state == Database.State.DISCONNECTING }

        assertEquals(
            0,
            database.disconnectCalls.get(),
            "the implementation must not be released while an operation still holds a lease"
        )

        operationGate.countDown()
        operation.await()
        disconnecting.await()

        assertEquals(1, database.disconnectCalls.get())
        assertFalse(database.isConnected)
        assertNull(Database.current)
    }

    @Test
    fun `operations started after disconnect begins are rejected`() = runBlocking<Unit> {
        val database = connectedDatabase()
        val operationGate = CountDownLatch(1)
        val operationStarted = CountDownLatch(1)

        val operation = async(Dispatchers.IO) {
            database.withQueries { _ ->
                operationStarted.countDown()
                operationGate.awaitOpened()
            }
        }
        operationStarted.awaitOpened()

        val disconnecting = async(Dispatchers.IO) { database.disconnect() }
        awaitUntil("the database enters DISCONNECTING") { database.state == Database.State.DISCONNECTING }

        assertFailsWith<IllegalStateException> { database.withQueries { } }

        operationGate.countDown()
        operation.await()
        disconnecting.await()
    }

    @Test
    fun `a failing operation still releases its lease`() = runBlocking<Unit> {
        val database = connectedDatabase()

        assertFailsWith<IllegalStateException> {
            database.withQueries { _ -> throw IllegalStateException("operation failed") }
        }

        val disconnecting = async(Dispatchers.IO) { database.disconnect() }
        awaitUntil("the disconnect completes") { database.disconnectCalls.get() == 1 }
        disconnecting.await()

        assertFalse(database.isConnected)
        assertNull(Database.current)
    }

    @Test
    fun `operations are rejected once the lifecycle is shutting down`() = runBlocking<Unit> {
        val database = connectedDatabase()

        Database.shutdown()

        assertFailsWith<IllegalStateException> { database.withQueries { } }
    }

    // ---------------------------------------------------------------- disconnecting

    @Test
    fun `disconnecting twice releases the implementation once`() = runBlocking<Unit> {
        val database = connectedDatabase()

        database.disconnect()
        database.disconnect()

        assertEquals(1, database.disconnectCalls.get())
        assertEquals(Database.State.DISCONNECTED, database.state)
        assertNull(database.queries)
    }

    @Test
    fun `disconnecting a database that never connected does nothing`() = runBlocking<Unit> {
        val database = ControllableDatabase("untouched")

        database.disconnect()

        assertEquals(0, database.disconnectCalls.get())
        assertEquals(Database.State.DISCONNECTED, database.state)
    }

    @Test
    fun `a disconnect failure still clears the published state`() = runBlocking<Unit> {
        val database = connectedDatabase()
        database.disconnectFailure = IllegalStateException("close failed")

        val thrown = assertFailsWith<IllegalStateException> { database.disconnect() }

        assertEquals("close failed", thrown.message)
        assertNull(Database.current)
        assertEquals(Database.State.DISCONNECTED, database.state)
        assertNull(database.queries)
    }

    // ---------------------------------------------------------------- shutdown and lifecycle

    @Test
    fun `shutdown disconnects and unpublishes the database`() = runBlocking<Unit> {
        val database = connectedDatabase()

        Database.shutdown()

        assertEquals(1, database.disconnectCalls.get())
        assertNull(Database.current)
        assertFalse(database.isConnected)
        assertTrue(Database.isShuttingDown)
    }

    @Test
    fun `repeated shutdown is safe`() = runBlocking<Unit> {
        val database = connectedDatabase()

        Database.shutdown()
        Database.shutdown()
        Database.shutdown()

        assertEquals(1, database.disconnectCalls.get())
        assertNull(Database.current)
        assertTrue(Database.isShuttingDown)
    }

    @Test
    fun `shutdown without a database still closes the lifecycle`() = runBlocking<Unit> {
        Database.shutdown()

        assertTrue(Database.isShuttingDown)
        assertNull(Database.current)
    }

    @Test
    fun `connectDefault is rejected while the lifecycle is shutting down`() = runBlocking<Unit> {
        val rejected = ControllableDatabase("rejected")
        Database.shutdown()

        assertFailsWith<IllegalStateException> {
            Database.connectDefault(rejected, SETTINGS)
        }

        assertNull(Database.current)
        assertEquals(0, rejected.connectCalls.get())
    }

    @Test
    fun `beginLifecycle refuses to start while a database is published`() = runBlocking<Unit> {
        connectedDatabase()

        assertFailsWith<IllegalStateException> { Database.beginLifecycle() }
    }

    @Test
    fun `beginLifecycle reopens the lifecycle after shutdown`() = runBlocking<Unit> {
        Database.shutdown()
        assertTrue(Database.isShuttingDown)

        Database.beginLifecycle()

        assertFalse(Database.isShuttingDown)
        val database = connectedDatabase("after-restart")
        assertTrue(database.isConnected)
    }

    @Test
    fun `shutdown during a connect attempt cancels the publish`() = runBlocking<Unit> {
        val database = ControllableDatabase("slow")
        val connectGate = CountDownLatch(1)
        database.connectGate = connectGate

        val connecting = async(Dispatchers.IO) {
            runCatching { Database.connectDefault(database, SETTINGS) }
        }
        database.connectEntered.awaitOpened()

        val shuttingDown = async(Dispatchers.IO) { Database.shutdown() }
        // Once the flag is observable the outcome is fixed: the connect can no longer publish,
        // because shutdown already holds the transition lock that connectDefault is waiting on.
        awaitUntil("the lifecycle starts shutting down") { Database.isShuttingDown }

        connectGate.countDown()

        val outcome = connecting.await()
        shuttingDown.await()

        assertTrue(outcome.isFailure, "connecting while the lifecycle shuts down must fail")
        assertEquals(
            "Database lifecycle began shutting down while connecting.",
            outcome.exceptionOrNull()?.message
        )
        assertNull(Database.current)
        assertEquals(Database.State.DISCONNECTED, database.state)
        assertNull(database.queries)
        assertEquals(1, database.disconnectCalls.get(), "the abandoned connect must release its resources")
    }

    // ---------------------------------------------------------------- concurrency

    @Test
    fun `concurrent connectDefault calls leave exactly one published database`() = runBlocking<Unit> {
        val databases = List(4) { ControllableDatabase("concurrent-$it") }

        databases.map { database ->
            async(Dispatchers.IO) { Database.connectDefault(database, SETTINGS) }
        }.awaitAll()

        val published = Database.current
        assertNotNull(published)
        assertEquals(1, databases.count { it === published }, "exactly one database may stay published")
        databases.filter { it !== published }.forEach { replaced ->
            assertEquals(1, replaced.disconnectCalls.get(), "$replaced must be released exactly once")
            assertFalse(replaced.isConnected)
        }
        assertEquals(0, (published as ControllableDatabase).disconnectCalls.get())
        assertTrue(published.isConnected)
        databases.forEach { assertEquals(1, it.connectCalls.get(), "$it must connect exactly once") }
    }

    @Test
    fun `concurrent shutdown calls release the implementation once`() = runBlocking<Unit> {
        val database = connectedDatabase()

        List(4) { async(Dispatchers.IO) { Database.shutdown() } }.awaitAll()

        assertEquals(1, database.disconnectCalls.get())
        assertNull(Database.current)
        assertTrue(Database.isShuttingDown)
    }

    @Test
    fun `concurrent registrations all land`() = runBlocking<Unit> {
        val database = connectedDatabase()
        val tables = List(8) { table("table_$it") }

        tables.map { entry ->
            async(Dispatchers.IO) { database.registerTable(entry) }
        }.awaitAll()

        assertEquals(tables.map { it.name }.toSet(), database.tables.keys)
        assertEquals(tables.map { it.name }.toSet(), database.registeredTables.toSet())
    }

    @Test
    fun `concurrent reads and writes are all drained before disconnect`() = runBlocking<Unit> {
        val database = connectedDatabase()
        val operationGate = CountDownLatch(1)
        val started = CountDownLatch(3)

        val operations = List(3) {
            async(Dispatchers.IO) {
                database.withQueries { _ ->
                    started.countDown()
                    operationGate.awaitOpened()
                }
            }
        }
        started.awaitOpened()

        val disconnecting = async(Dispatchers.IO) { database.disconnect() }
        awaitUntil("the database enters DISCONNECTING") { database.state == Database.State.DISCONNECTING }
        assertEquals(0, database.disconnectCalls.get())

        operationGate.countDown()
        operations.awaitAll()
        disconnecting.await()

        assertEquals(1, database.disconnectCalls.get())
        assertNull(Database.current)
    }

    @Test
    fun `concurrent connectDefault and shutdown never corrupt the lifecycle`() = runBlocking<Unit> {
        val databases = List(3) { ControllableDatabase("race-$it") }

        val replacements = databases.map { database ->
            async(Dispatchers.IO) {
                runCatching { Database.connectDefault(database, SETTINGS) }
            }
        }
        val shutdowns = List(3) { async(Dispatchers.IO) { Database.shutdown() } }

        val outcomes = replacements.awaitAll()
        shutdowns.awaitAll()

        outcomes.mapNotNull { it.exceptionOrNull() }.forEach { error ->
            assertTrue(
                error is IllegalStateException,
                "a losing transition may only fail with the documented rejection, but was $error"
            )
        }
        assertTrue(
            databases.none { it.isConnected },
            "no database may stay connected once every shutdown has completed"
        )
        assertNull(Database.current)
        assertTrue(Database.isShuttingDown)
    }

    // ---------------------------------------------------------------- named connections

    @Test
    fun `a named connection is registered without replacing the default`() = runBlocking<Unit> {
        val unnamed = connectedDatabase("default")

        val named = connectedDatabase(named = "logs")

        assertSame(unnamed, Database.current)
        assertSame(named, Database.connection("logs"))
        assertEquals(listOf("logs"), Database.connectionNames)
        assertEquals(0, unnamed.disconnectCalls.get())
        assertEquals(0, named.disconnectCalls.get())
    }

    @Test
    fun `the first connection becomes the default`() = runBlocking<Unit> {
        val named = connectedDatabase(named = "logs")

        assertSame(named, Database.current)
        assertSame(named, Database.connection("logs"))
    }

    @Test
    fun `a named connection can be made the default`() = runBlocking<Unit> {
        val first = connectedDatabase("first")
        val second = connectedDatabase(named = "logs")

        assertTrue(Database.makeDefault("logs"))
        assertSame(second, Database.current)
        assertFalse(Database.makeDefault("missing"))
        assertSame(second, Database.current)
        assertEquals(0, first.disconnectCalls.get())
    }

    @Test
    fun `an unnamed connection replaces the default role but keeps a named connection alive`() =
        runBlocking<Unit> {
            val named = connectedDatabase(named = "logs")

            val unnamed = connectedDatabase("default")

            assertSame(unnamed, Database.current)
            assertSame(named, Database.connection("logs"))
            assertEquals(0, named.disconnectCalls.get())

            // Still reachable by name, so its operations are still accepted.
            named.withQueries { }
        }

    @Test
    fun `an unknown name is not a connection`() = runBlocking<Unit> {
        connectedDatabase(named = "logs")

        assertNull(Database.connection("archive"))
        assertEquals(listOf("logs"), Database.connectionNames)
        assertFalse(Database.makeDefault("archive"))
    }

    @Test
    fun `reconnecting a name replaces only that connection`() = runBlocking<Unit> {
        val other = connectedDatabase(named = "main")
        val first = connectedDatabase(named = "logs")

        val second = connectedDatabase(named = "logs")

        assertEquals(1, first.disconnectCalls.get())
        assertFalse(first.isConnected)
        assertSame(second, Database.connection("logs"))
        assertSame(other, Database.connection("main"))
        assertEquals(0, other.disconnectCalls.get())
        assertEquals(listOf("logs", "main"), Database.connectionNames)
    }

    @Test
    fun `disconnecting a named connection leaves the others alone`() = runBlocking<Unit> {
        val first = connectedDatabase(named = "logs")
        val second = connectedDatabase(named = "main")

        first.disconnect()

        assertNull(Database.connection("logs"))
        assertEquals(listOf("main"), Database.connectionNames)
        assertTrue(second.isConnected)
    }

    @Test
    fun `disconnectAll closes every connection and keeps the lifecycle open`() = runBlocking<Unit> {
        val default = connectedDatabase("default")
        val named = connectedDatabase(named = "logs")

        Database.disconnectAll()

        assertEquals(1, default.disconnectCalls.get())
        assertEquals(1, named.disconnectCalls.get())
        assertNull(Database.current)
        assertTrue(Database.connectionNames.isEmpty())
        assertFalse(Database.isShuttingDown)
    }

    @Test
    fun `shutdown closes every connection`() = runBlocking<Unit> {
        val default = connectedDatabase("default")
        val named = connectedDatabase(named = "logs")

        Database.shutdown()

        assertEquals(1, default.disconnectCalls.get())
        assertEquals(1, named.disconnectCalls.get())
        assertNull(Database.current)
        assertTrue(Database.connectionNames.isEmpty())
        assertTrue(Database.isShuttingDown)
    }

    @Test
    fun `beginLifecycle refuses to start while a named connection is registered`() = runBlocking<Unit> {
        connectedDatabase(named = "logs")

        assertFailsWith<IllegalStateException> { Database.beginLifecycle() }
    }

    @Test
    fun `a named connection stays out of the registry when connecting fails`() = runBlocking<Unit> {
        val database = ControllableDatabase("broken")
        database.connectFailure = IllegalStateException("refused")

        assertFailsWith<IllegalStateException> {
            Database.connectNamed("logs", database, SETTINGS)
        }

        assertNull(Database.connection("logs"))
        assertTrue(Database.connectionNames.isEmpty())
        assertNull(Database.current)
    }

    // ---------------------------------------------------------------- draining

    /**
     * The wait for leases exists so the pool is not closed under a running statement, but a lease is
     * released by that statement's own code finishing, and nothing here can interrupt a blocking JDBC
     * call. Without a bound of its own, one stuck operation would hang disconnect, and with it the
     * plugin's own disable, which is a server that cannot be stopped.
     */
    @Test
    fun `a disconnect that cannot drain says so and closes anyway`() = runBlocking<Unit> {
        val database = connectedDatabase()
        val entered = CompletableDeferred<Unit>()
        val released = CompletableDeferred<Unit>()
        val stuck = async(Dispatchers.IO) {
            runCatching {
                database.withQueries { _ ->
                    entered.complete(Unit)
                    released.await()
                }
            }
        }
        entered.await()

        val warnings = CopyOnWriteArrayList<String>()
        Database.warn = { message -> warnings += message }
        database.drainTimeout = Duration.ofMillis(100)

        withTimeout(GATE_TIMEOUT_SECONDS * 1000) { database.disconnect() }

        assertFalse(database.isConnected)
        assertEquals(1, warnings.size)
        assertTrue(
            "1 database operation(s) are still running" in warnings.single(),
            "unexpected warning: ${warnings.single()}"
        )

        // The operation that was still running releases its lease afterwards. That has to be harmless:
        // the state machine is already disconnected, and the late release only decrements a counter.
        released.complete(Unit)
        assertTrue(stuck.await().isSuccess)
        assertEquals(Database.State.DISCONNECTED, database.state)
        assertNull(Database.current)
    }

    // ---------------------------------------------------------------- helpers

    private suspend fun connectedDatabase(
        label: String = "database",
        named: String? = null
    ): ControllableDatabase {
        val database = ControllableDatabase(label)
        if (named == null) {
            Database.connectDefault(database, SETTINGS)
        } else {
            Database.connectNamed(named, database, SETTINGS)
        }
        return database
    }

    /** Restores the global lifecycle so the next test starts from a clean, open state. */
    private fun resetLifecycle() = runBlocking {
        // Unconditional, because a named connection can outlive the default one: `current` being null
        // does not mean nothing is registered.
        Database.shutdown()
        Database.beginLifecycle()
        // Process-global, so a test that installs one has to remove it again.
        Database.warn = {}
    }
}

private fun table(name: String, vararg columns: String): Table {
    val names = columns.ifEmpty { arrayOf("id") }
    return Table(name, names.map { Column(it, IntDataType(), isPrimaryKey = it == "id") })
}

private fun CountDownLatch.awaitOpened() {
    check(await(GATE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        "A test gate was not opened within $GATE_TIMEOUT_SECONDS s."
    }
}

private fun awaitUntil(description: String, condition: () -> Boolean) {
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(GATE_TIMEOUT_SECONDS)
    while (System.nanoTime() < deadline) {
        if (condition()) return
        Thread.sleep(5)
    }
    check(condition()) { "Timed out waiting until $description." }
}

/**
 * A [Database] whose transitions can be held open, made to fail, and inspected afterwards.
 *
 * [doConnect] and [doDisconnect] are not suspending, so a gate blocks the calling thread; the tests
 * therefore drive the lifecycle from [Dispatchers.IO] when they need a transition to stay in flight.
 */
private class ControllableDatabase(private val label: String) : Database() {

    override val dataTypes: DataTypes = NoDataTypes

    /** Settable so the test that cannot drain does not wait the production default out. */
    override var drainTimeout: Duration = DEFAULT_DRAIN_TIMEOUT

    val connectEntered = CountDownLatch(1)
    val disconnectEntered = CountDownLatch(1)

    @Volatile
    var connectGate: CountDownLatch? = null

    @Volatile
    var disconnectGate: CountDownLatch? = null

    @Volatile
    var connectFailure: Throwable? = null

    @Volatile
    var disconnectFailure: Throwable? = null

    @Volatile
    var registerFailure: Throwable? = null

    @Volatile
    var publishQueries: Boolean = true

    val connectCalls = AtomicInteger()
    val disconnectCalls = AtomicInteger()
    val registeredTables = CopyOnWriteArrayList<String>()

    override fun doConnect(settings: ConnectionSettings) {
        connectCalls.incrementAndGet()
        connectEntered.countDown()
        connectGate?.awaitOpened()
        connectFailure?.let { throw it }
        if (publishQueries) queries = UnusedQueries
    }

    override fun doDisconnect() {
        disconnectCalls.incrementAndGet()
        disconnectEntered.countDown()
        disconnectGate?.awaitOpened()
        disconnectFailure?.let { throw it }
    }

    override suspend fun doRegisterTable(table: Table) {
        registerFailure?.let { throw it }
        registeredTables.add(table.name)
    }

    override fun toString(): String = "ControllableDatabase($label)"
}

private object NoDataTypes : DataTypes() {
    override val typesRegistry = mutableMapOf<TypeId, DataType<*>>()
}

/** Satisfies the lifecycle's "queries must be initialized" check; no test here executes a query. */
private object UnusedQueries : Queries {

    override fun selectById(id: Any): SelectById = unusedQuery()
    override fun selectOne(where: WhereClause?): SelectOne = unusedQuery()
    override fun selectMany(where: WhereClause?): SelectMany = unusedQuery()
    override fun selectPage(pageSize: Int, pageIndex: Int, where: WhereClause?): SelectPage = unusedQuery()
    override fun insertOne(values: Map<String, Any?>): InsertOne = unusedQuery()
    override fun insertMany(valuesList: List<Map<String, Any?>>): InsertMany = unusedQuery()
    override fun insertIfAbsent(values: Map<String, Any?>): InsertIfAbsent = unusedQuery()
    override fun update(values: Map<String, Any?>, limit: Int?, where: WhereClause?): Update = unusedQuery()
    override fun updateById(id: Any, values: Map<String, Any?>): UpdateById = unusedQuery()
    override fun upsertById(id: Any, values: Map<String, Any?>): UpsertById = unusedQuery()
    override fun delete(limit: Int?, where: WhereClause?): Delete = unusedQuery()
    override fun deleteById(id: Any): DeleteById = unusedQuery()
}

private fun unusedQuery(): Nothing =
    throw UnsupportedOperationException("The lifecycle tests never execute a query.")
