package io.github.heyhey123.skriptorm.impl.mongo.integration

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.impl.mongo.database.MongodbDatabase
import io.github.heyhey123.skriptorm.impl.mongo.type.MongoDataTypes
import io.github.heyhey123.skriptorm.queries.Queries
import io.github.heyhey123.skriptorm.result.CursorResult
import io.github.heyhey123.skriptorm.result.DataCursor
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.DataType
import io.github.heyhey123.skriptorm.type.TypeId
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.util.UUID
import kotlin.test.fail

/**
 * Base class for integration tests that exercise the MongoDB implementation against a real server.
 *
 * Every test gets its own connection and its own lifecycle, so a test that fails mid-flight cannot leave
 * the global [Database] state behind for the next one. Tests own their collections and may drop them freely.
 *
 * The columns below are declared with [MongoDataTypes] rather than with another implementation's types,
 * because MongoDB is the implementation that decides what a value is stored as: a uuid is `Binary` here.
 * Reading a row back uses the same objects, since a value is converted the way its column was.
 *
 * A second client of the test's own reads what MongoDB actually holds. A count or an index list taken
 * through the implementation would only say that it agrees with itself.
 *
 * Test methods are written as `= runBlocking<Unit> { ... }`. JUnit only discovers `@Test` methods that
 * return `void`, while helpers such as `assertFailsWith` return a value, so the explicit type argument
 * stops an expression-bodied test from silently vanishing from the run.
 */
abstract class MongoIntegrationTestBase {

    protected lateinit var database: MongodbDatabase
        private set

    /** Query factory created by [MongodbDatabase] for this test's connection. */
    protected val queries: Queries
        get() = checkNotNull(database.queries) { "The database did not initialize its queries." }

    private var verificationClient: MongoClient? = null

    /** The same database, read without the implementation in the way. */
    protected val verification: MongoCollection<Document>
        get() = collectionOf(usersTable.name)

    private lateinit var databaseName: String

    protected val intType = mongoType<Int>(TypeId.INT)
    protected val stringType = mongoType<String>(TypeId.STRING)
    protected val doubleType = mongoType<Double>(TypeId.DOUBLE)
    protected val uuidType = mongoType<UUID>(TypeId.UUID)

    /**
     * The table every example in this suite shares: an auto-increment primary key plus one column for
     * each driver-independent type the implementation stores.
     */
    protected val usersTable = Table(
        "orm_users",
        listOf(
            Column("id", intType, isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
            Column("name", stringType, isNullable = false),
            Column("age", intType),
            Column("score", doubleType),
            Column("uid", uuidType)
        )
    )

    @BeforeEach
    fun openDatabase() {
        val endpoint = MongoTestServer.requireEndpoint()
        runBlocking {
            // A previous test class may have aborted before its teardown ran. Shutting down is not
            // conditional on there being a default connection: a named one can outlive it.
            Database.shutdown()
            Database.beginLifecycle()
            val opened = MongodbDatabase(endpoint.properties)
            Database.connectDefault(opened, endpoint.settings)
            database = opened
        }
        databaseName = endpoint.database
        verificationClient = MongoClients.create(endpoint.url)
    }

    @AfterEach
    fun closeDatabase() {
        verificationClient?.close()
        verificationClient = null
        runBlocking { Database.shutdown() }
    }

    /** Drops [table] and registers it again, so a test starts from an empty, fully typed collection. */
    protected suspend fun recreateTable(table: Table = usersTable) {
        collectionOf(table.name).drop()
        database.registerTable(table)
    }

    /** The collection [name] of the database under test, read without the implementation in the way. */
    protected fun collectionOf(name: String): MongoCollection<Document> =
        checkNotNull(verificationClient) { "The verification client is not open." }
            .getDatabase(databaseName)
            .getCollection(name)

    /** How many documents [name] holds, counted by MongoDB itself. */
    protected fun rawCount(name: String = usersTable.name): Long = collectionOf(name).countDocuments()

    /** The indexes MongoDB reports for [name]. */
    protected fun indexesOf(name: String = usersTable.name): List<Document> =
        collectionOf(name).listIndexes().toList()

    /** Every collection of the database under test, as MongoDB names them. */
    protected fun collectionNames(): List<String> =
        checkNotNull(verificationClient) { "The verification client is not open." }
            .getDatabase(databaseName)
            .listCollectionNames()
            .toList()

    /** Every row of [table]. */
    protected suspend fun allUsers(table: Table = usersTable): List<UserRow> =
        queries.selectMany(null).execute(table).readUsers()

    /** One row of [usersTable], typed the same way the table is declared. */
    protected data class UserRow(
        val id: Int,
        val name: String,
        val age: Int?,
        val score: Double?,
        val uid: UUID?
    )

    /** Reads the current row of this cursor as a [UserRow]. */
    protected fun DataCursor.readUser(): UserRow = UserRow(
        id = requireNotNull(get("id", intType)) { "id must not be null" },
        name = requireNotNull(get("name", stringType)) { "name must not be null" },
        age = get("age", intType),
        score = get("score", doubleType),
        uid = get("uid", uuidType)
    )

    /** Drains this result into rows and always closes the cursor. */
    protected fun CursorResult.readUsers(): List<UserRow> = cursor.use { open ->
        buildList { while (open.next()) add(open.readUser()) }
    }

    /**
     * Builds an insert map for [usersTable]. `id` is omitted when null so the table's counter generates
     * the next value; every other column is written explicitly, including a null.
     */
    protected fun userValues(
        name: String,
        id: Int? = null,
        age: Int? = null,
        score: Double? = null,
        uid: UUID? = null
    ): Map<String, Any?> = linkedMapOf<String, Any?>().apply {
        id?.let { put("id", it) }
        put("name", name)
        put("age", age)
        put("score", score)
        put("uid", uid)
    }

    /**
     * The type this implementation stores [id] as.
     *
     * The registry holds the MongoDB-specific types, so a test that declared a column with anything else
     * would be testing a table this implementation never creates. A suite with a table of its own, for the
     * types the shared one does not declare, reads them the same way through this.
     */
    protected fun <T : Any> mongoType(id: TypeId): DataType<T> {
        val type = requireNotNull(MongoDataTypes.typesRegistry[id]) {
            "The MongoDB implementation declares no type for $id."
        }
        @Suppress("UNCHECKED_CAST")
        return type as DataType<T>
    }

    /**
     * The [IllegalArgumentException] [block] threw.
     *
     * A statement is refused before it is sent, and `assertFailsWith` takes a lambda that cannot suspend,
     * which is what a statement's `execute` is.
     */
    protected suspend fun failing(block: suspend () -> Unit): IllegalArgumentException =
        failingAs(IllegalArgumentException::class.java, block)

    /**
     * The [T] [block] threw, for a statement that fails for a reason of its own rather than being refused
     * before it is sent.
     */
    protected suspend fun <T : Throwable> failingAs(type: Class<T>, block: suspend () -> Unit): T = try {
        block()
        fail("Expected the call to be refused.")
    } catch (error: Throwable) {
        if (type.isInstance(error)) type.cast(error) else throw error
    }
}
