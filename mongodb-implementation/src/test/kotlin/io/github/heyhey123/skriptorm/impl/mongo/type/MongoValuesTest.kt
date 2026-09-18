package io.github.heyhey123.skriptorm.impl.mongo.type

import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.IntDataType
import io.github.heyhey123.skriptorm.type.SkriptDate
import io.github.heyhey123.skriptorm.type.SkriptTime
import io.github.heyhey123.skriptorm.type.SkriptTimespan
import io.github.heyhey123.skriptorm.type.StringDataType
import org.bson.types.Binary
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Covers the conversion of the values a statement carries into what a document holds, without a server.
 *
 * A converter that is never called is the kind of mistake that only shows up against a real collection,
 * and then as a driver error about a class it cannot encode rather than as a wrong value. These say that
 * every write and every filter goes through the converter, and that a column the table does not declare,
 * or a value the column cannot hold, is refused before anything is sent.
 */
class MongoValuesTest {

    private val table = Table(
        "users",
        listOf(
            Column("id", IntDataType(), isPrimaryKey = true),
            Column("name", StringDataType()),
            Column("uid", UuidMongoDataType),
            Column("amount", TinyIntMongoDataType),
            Column("ratio", FloatMongoDataType),
            Column("joined", SkriptDateMongoDataType),
            Column("clock", SkriptTimeMongoDataType),
            Column("took", SkriptTimespanMongoDataType)
        )
    )

    @Test
    fun `a document holds what each column's converter produces`() {
        val uuid = UUID.fromString("6f9619ff-8b86-d011-b42d-00cf4fc964ff")

        val document = MongoValues.storageDocument(
            table,
            mapOf("id" to 1, "name" to "first", "uid" to uuid, "amount" to 7.toByte(), "ratio" to 1.5f)
        )

        assertEquals(1, document["id"])
        assertEquals("first", document["name"])
        assertEquals(UuidMongoConverter.toStorage(uuid), document["uid"])
        assertEquals(7, document["amount"], "a tinyint is stored as the whole number int32 holds")
        assertEquals(1.5, document["ratio"], "a float is stored as the double double holds")
    }

    @Test
    fun `a uuid survives the round trip through its stored bytes`() {
        val uuid = UUID.fromString("6f9619ff-8b86-d011-b42d-00cf4fc964ff")

        val stored = assertIs<Binary>(MongoValues.storage(table, "uid", uuid))

        assertEquals(uuid, UuidMongoConverter.fromStorage(stored))
    }

    @Test
    fun `a null is stored as null and nothing is converted`() {
        assertNull(MongoValues.storage(table, "uid", null))
    }

    @Test
    fun `the skript backed types are stored as the numbers mongodb has for them`() {
        val document = MongoValues.storageDocument(
            table,
            mapOf(
                "joined" to SkriptDate(1_700_000_000_000L),
                "clock" to SkriptTime(6000),
                "took" to SkriptTimespan(90_000L)
            )
        )

        assertEquals(1_700_000_000_000L, document["joined"], "a date is the epoch milliseconds")
        assertEquals(6000, document["clock"], "a time is the ticks of the day")
        assertEquals(90_000L, document["took"], "a timespan is its milliseconds")
    }

    @Test
    fun `a stored skript value comes back as the value it was`() {
        val moment = SkriptDate(1_700_000_000_000L)
        val time = SkriptTime(6000)
        val span = SkriptTimespan(90_000L)

        val stored = MongoValues.storageDocument(
            table,
            mapOf("joined" to moment, "clock" to time, "took" to span)
        )

        assertEquals(
            moment.time,
            SkriptDateMongoConverter.fromStorage(stored.getValue("joined") as Long).time
        )
        assertEquals(
            time.ticks,
            SkriptTimeMongoConverter.fromStorage(stored.getValue("clock") as Int).ticks
        )
        assertEquals(
            span.duration.toMillis(),
            SkriptTimespanMongoConverter.fromStorage(stored.getValue("took") as Long).duration.toMillis()
        )
    }

    @Test
    fun `a column the table does not declare is refused`() {
        val error = assertFailsWith<IllegalArgumentException> { MongoValues.storage(table, "nope", 1) }

        assertEquals("Table users does not have column nope.", error.message)
    }

    @Test
    fun `a value the column cannot hold is refused`() {
        val error = assertFailsWith<IllegalArgumentException> { MongoValues.storage(table, "id", "1") }

        assertEquals(
            "Value for type int must be java.lang.Integer, but was java.lang.String.",
            error.message
        )
    }
}
