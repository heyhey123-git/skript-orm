package io.github.heyhey123.skriptorm.impl.mongo.integration

import io.github.heyhey123.skriptorm.table.Column
import io.github.heyhey123.skriptorm.table.Table
import io.github.heyhey123.skriptorm.type.SkriptDate
import io.github.heyhey123.skriptorm.type.SkriptTime
import io.github.heyhey123.skriptorm.type.SkriptTimespan
import io.github.heyhey123.skriptorm.type.TypeId
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Proves that the types the shared table does not declare survive the whole path — converter, BSON, the
 * server, and the typed cursor read — rather than only the converter they pass through in a unit test.
 *
 * The shared table covers what every implementation stores without help: whole and fractional numbers, text
 * and a uuid. These are the rest of what this JVM can build: the Skript-sided types, whose converters turn
 * them into numbers MongoDB has, and the two scalar types whose storage is wider than their domain (a byte
 * becomes an int32, a float becomes a double). A conversion that widens is exactly the kind that looks
 * right in a unit test and loses a value on the way back, so it is worth a server.
 *
 * `itemstack`, `location` and `bukkitserializable` are not here: building one needs a running server, and
 * `nbtcompound` needs SkBee, which is deliberately absent. Those are covered where a server exists, by the
 * addon-level round trip of the Skript server test.
 */
class MongoTypeRoundTripIntegrationTest : MongoIntegrationTestBase() {

    private val tinyType = mongoType<Byte>(TypeId.TINYINT)
    private val floatType = mongoType<Float>(TypeId.FLOAT)
    private val dateType = mongoType<SkriptDate>(TypeId.DATE)
    private val timeType = mongoType<SkriptTime>(TypeId.TIME)
    private val timespanType = mongoType<SkriptTimespan>(TypeId.TIMESPAN)

    private val valuesTable = Table(
        "orm_values",
        listOf(
            Column("id", intType, isPrimaryKey = true, isAutoIncrement = true, isNullable = false),
            Column("tiny", tinyType),
            Column("ratio", floatType),
            Column("joined", dateType),
            Column("clock", timeType),
            Column("took", timespanType)
        )
    )

    @Test
    fun `a byte and a float keep their exact values`() = runBlocking<Unit> {
        recreateTable(valuesTable)
        queries.insertOne(
            mapOf("tiny" to Byte.MIN_VALUE, "ratio" to 2.25f)
        ).execute(valuesTable)
        queries.insertOne(
            mapOf("tiny" to Byte.MAX_VALUE, "ratio" to -1.5f)
        ).execute(valuesTable)

        val rows = queries.selectMany(null).execute(valuesTable).cursor.use { cursor ->
            buildList {
                while (cursor.next()) {
                    add(cursor.get("tiny", tinyType) to cursor.get("ratio", floatType))
                }
            }
        }.sortedBy { it.first }

        assertEquals(listOf(Byte.MIN_VALUE to 2.25f, Byte.MAX_VALUE to -1.5f), rows)
    }

    @Test
    fun `the skript sided types come back as the values they were`() = runBlocking<Unit> {
        recreateTable(valuesTable)
        val moment = SkriptDate(1_700_000_000_000L)
        val time = SkriptTime(6000)
        val span = SkriptTimespan(90_000L)

        queries.insertOne(
            mapOf("joined" to moment, "clock" to time, "took" to span)
        ).execute(valuesTable)

        val read = queries.selectMany(null).execute(valuesTable).cursor.use { cursor ->
            assertTrue(cursor.next(), "the row just written must be readable")
            Triple(
                assertNotNull(cursor.get("joined", dateType)),
                assertNotNull(cursor.get("clock", timeType)),
                assertNotNull(cursor.get("took", timespanType))
            )
        }

        // Compared through what each type carries rather than by equality, which Skript's own types are not
        // required to implement: the milliseconds, the ticks of the day and the duration are what a script
        // reads, and they are what the converters write.
        assertEquals(moment.time, read.first.time)
        assertEquals(time.ticks, read.second.ticks)
        assertEquals(span.duration, read.third.duration)
    }
}
