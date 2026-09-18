package io.github.heyhey123.skriptorm.impl.mongo.integration

import com.mongodb.client.model.Filters
import io.github.heyhey123.skriptorm.impl.mongo.database.MongoSequences
import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * What registering a table leaves on the server, read from the server rather than assumed.
 *
 * A collection needs no declaration the way a table does: MongoDB creates it with the first document.
 * What registration can add is the unique index that makes the primary key a key, and the counter of an
 * auto-increment column, since neither can be added by a later write. Both are read back here, together
 * with the two names a collection cannot carry.
 */
class MongoSchemaIntegrationTest : MongoIntegrationTestBase() {

    @Test
    fun `registering a table creates a unique index on its primary key`() = runBlocking<Unit> {
        recreateTable()

        val keyIndex = indexesOf().single { it["name"] == "${usersTable.primaryKey!!.name}_1" }

        assertEquals(Document("id", 1), keyIndex["key"])
        assertEquals(true, keyIndex["unique"])
    }

    @Test
    fun `registering an auto-increment table creates the counter of its key`() = runBlocking<Unit> {
        collectionOf(MongoSequences.COLLECTION).drop()
        recreateTable()

        val counter = collectionOf(MongoSequences.COLLECTION)
            .find(Filters.eq("_id", "${usersTable.name}.${usersTable.primaryKey!!.name}"))
            .first()

        assertEquals(0L, counter?.get("value"), "the first value handed out is one")
    }

    @Test
    fun `registering the same table twice changes nothing`() = runBlocking<Unit> {
        collectionOf(MongoSequences.COLLECTION).drop()
        recreateTable()
        val indexes = indexesOf().map { it["name"] }.toSet()
        val counter = collectionOf(MongoSequences.COLLECTION).find().first()

        database.registerTable(usersTable)

        assertEquals(indexes, indexesOf().map { it["name"] }.toSet())
        assertEquals(counter, collectionOf(MongoSequences.COLLECTION).find().first())
    }

    @Test
    fun `a table may not declare a column named after the document identifier`() = runBlocking<Unit> {
        val table = Table("orm_reserved", listOf(Column("_id", intType, isPrimaryKey = true)))

        val error = failing { database.registerTable(table) }

        assertEquals(
            "Table orm_reserved cannot declare a column called '_id': " +
                "MongoDB reserves that name for the identifier of the document itself.",
            error.message
        )
    }

    @Test
    fun `a table may not be named after the counter collection`() = runBlocking<Unit> {
        val table = Table(MongoSequences.COLLECTION, listOf(Column("id", intType, isPrimaryKey = true)))

        val error = failing { database.registerTable(table) }

        assertEquals(
            "Table ${MongoSequences.COLLECTION} cannot be named '${MongoSequences.COLLECTION}': " +
                "that collection holds the counters of auto-increment columns.",
            error.message
        )
    }

    @Test
    fun `registering a table raises the counter past the keys already stored`() = runBlocking<Unit> {
        collectionOf(usersTable.name).drop()
        collectionOf(MongoSequences.COLLECTION).drop()
        collectionOf(usersTable.name).insertOne(Document("id", 500).append("name", "written outside"))

        database.registerTable(usersTable)
        queries.insertOne(userValues(name = "first")).execute(usersTable)

        assertTrue(
            allUsers().single { it.name == "first" }.id > 500,
            "a row that was there before the table was registered holds a key the counter must not reuse"
        )
    }

    @Test
    fun `registering a table again repairs a counter that was removed`() = runBlocking<Unit> {
        recreateTable()
        queries.insertOne(userValues(name = "first", id = 7)).execute(usersTable)
        collectionOf(MongoSequences.COLLECTION).drop()

        // What the failure message tells a reader to do when the stored rows are worth keeping: the counter
        // is rebuilt above the highest key the collection holds, never at zero.
        database.registerTable(usersTable)
        queries.insertOne(userValues(name = "second")).execute(usersTable)

        assertTrue(allUsers().single { it.name == "second" }.id > 7)
    }

    @Test
    fun `a counter someone removed is reported rather than counted from zero`() = runBlocking<Unit> {
        recreateTable()
        queries.insertOne(userValues(name = "first")).execute(usersTable)
        collectionOf(MongoSequences.COLLECTION).drop()

        // The counter is not the data, so losing it cannot be repaired from the data without racing: the
        // statement says what happened and what to set, instead of starting over and colliding with the
        // rows that are still there.
        val error = failingAs(IllegalStateException::class.java) {
            queries.insertOne(userValues(name = "second")).execute(usersTable)
        }

        assertEquals(
            "The counter of column id of table orm_users is missing, or holds no number. " +
                "Set its 'value' field to the highest key already in use, or register the table again " +
                "if the collection is empty.",
            error.message
        )
    }

    @Test
    fun `a table with neither a key nor a counter is not created`() = runBlocking<Unit> {
        val table = Table("orm_keyless", listOf(Column("name", stringType)))
        collectionOf(table.name).drop()

        database.registerTable(table)

        assertFalse(table.name in collectionNames(), "MongoDB creates a collection with its first document")
    }
}
