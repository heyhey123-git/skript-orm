package io.github.heyhey123.skriptorm.impl.mongo.result

import io.github.heyhey123.skriptorm.impl.mongo.type.TinyIntMongoDataType
import io.github.heyhey123.skriptorm.type.IntDataType
import io.github.heyhey123.skriptorm.type.StringDataType
import org.bson.Document
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Covers how a document a select already read is walked, without a server.
 *
 * The cursor is where the core reads every row of `select many` and `select page`, so an off-by-one here
 * makes those statements fail on every result rather than on an unusual one, and a document's own field
 * order is not the table's: `Document` keeps the order fields were appended in, which is the order the
 * insert happened to write them in, not the order the table declares its columns.
 */
class MongoDataCursorTest {

    @Test
    fun `a cursor walks every document once and then stops`() {
        val cursor = MongoDataCursor(
            listOf(
                Document("id", 1).append("name", "first"),
                Document("id", 2).append("name", "second")
            ),
            listOf("id", "name")
        )

        val names = mutableListOf<String?>()
        while (cursor.next()) {
            names += cursor.get("name", StringDataType())
        }

        assertEquals(listOf<String?>("first", "second"), names)
        assertFalse(cursor.next(), "a drained cursor has no further row")
    }

    @Test
    fun `a result with no document never has a row`() {
        assertFalse(MongoDataCursor(emptyList(), listOf("id")).next())
    }

    @Test
    fun `a column the document does not carry reads as null`() {
        val cursor = MongoDataCursor(listOf(Document("id", 1)), listOf("id", "name"))

        assertTrue(cursor.next())
        assertNull(cursor.get("name", StringDataType()))
    }

    @Test
    fun `an index reads the table's declaration order, not the document's`() {
        val cursor = MongoDataCursor(
            listOf(Document("name", "first").append("id", 1)),
            listOf("id", "name")
        )

        assertTrue(cursor.next())
        assertEquals(1, cursor.get(1, IntDataType()))
        assertEquals("first", cursor.get(2, StringDataType()))
    }

    @Test
    fun `a stored value is read through the column's converter`() {
        val cursor = MongoDataCursor(listOf(Document("amount", 7)), listOf("amount"))

        assertTrue(cursor.next())
        assertEquals(7.toByte(), cursor.get("amount", TinyIntMongoDataType))
    }
}
