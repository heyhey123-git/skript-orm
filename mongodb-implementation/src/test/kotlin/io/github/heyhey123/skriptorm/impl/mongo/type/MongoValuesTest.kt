package io.github.heyhey123.skriptorm.impl.mongo.type

import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.IntDataType
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
            Column("ratio", FloatMongoDataType)
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
