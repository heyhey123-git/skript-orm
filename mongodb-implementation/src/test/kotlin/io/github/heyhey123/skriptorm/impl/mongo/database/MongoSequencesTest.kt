package io.github.heyhey123.skriptorm.impl.mongo.database

import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import io.github.heyhey123.skriptorm.impl.mongo.type.FloatMongoDataType
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.IntDataType
import io.github.heyhey123.skriptorm.type.StringDataType
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.fail

/**
 * Covers what the counter can be asked without a server: the domain type an allocated value takes, the
 * case in which an insert is not touched at all, and the one auto-increment declaration that is refused
 * before anything is sent.
 *
 * Everything else about a counter talks to a collection — allocating a value, raising it to a key the
 * statement brought, rebuilding it when the table is registered — and is covered by the integration
 * suite, where a server exists.
 *
 * The database is a real client that is never used to send anything: a driver connects lazily, and the
 * cases here return before the first call, so nothing waits for a server.
 */
class MongoSequencesTest {

    @Test
    fun `the allocated value takes the domain type of the column`() {
        assertEquals(5.toByte(), MongoSequences.value(5, Byte::class.javaObjectType))
        assertEquals(5, MongoSequences.value(5, Int::class.javaObjectType))
        assertEquals(5L, MongoSequences.value(5, Long::class.javaObjectType))
    }

    @Test
    fun `a table whose key is not auto-increment is left untouched`() = withDatabase { database ->
        val values = mapOf("id" to 1, "name" to "first")

        assertSame(values, MongoSequences.withKey(database, plainTable, values))
    }

    @Test
    fun `an auto-increment column that cannot hold a whole number is refused`() = withDatabase { database ->
        val counters = Table(
            "counters",
            listOf(Column("id", FloatMongoDataType, isPrimaryKey = true, isAutoIncrement = true))
        )

        val error = failing { MongoSequences.prepare(database, counters) }

        assertEquals(
            "Auto-increment column id of table counters must hold whole numbers, but is declared as float.",
            error.message
        )
    }

    private val plainTable = Table(
        "users",
        listOf(
            Column("id", IntDataType(), isPrimaryKey = true),
            Column("name", StringDataType())
        )
    )

    private fun <T> withDatabase(block: suspend (MongoDatabase) -> T): T = runBlocking {
        val client = MongoClients.create(MongoClientSettings.builder().build())
        try {
            block(client.getDatabase("skript_orm_test"))
        } finally {
            client.close()
        }
    }

    private suspend fun failing(block: suspend () -> Unit): IllegalArgumentException = try {
        block()
        fail("Expected the call to be refused.")
    } catch (error: IllegalArgumentException) {
        error
    }
}
