package io.github.heyhey123.xiaojieorm.impl.jdbc.integration

import io.github.heyhey123.xiaojieorm.impl.jdbc.database.MysqlJdbcDialect
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.DoubleJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.IntJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.ItemStackJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.LocationJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.SkriptDateJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.SkriptTimeJdbcDataType
import io.github.heyhey123.xiaojieorm.impl.jdbc.type.SkriptTimespanJdbcDataType
import io.github.heyhey123.xiaojieorm.result.CursorResult
import io.github.heyhey123.xiaojieorm.result.DataCursor
import io.github.heyhey123.xiaojieorm.table.Column
import io.github.heyhey123.xiaojieorm.table.Table
import io.github.heyhey123.xiaojieorm.type.SkriptDate
import io.github.heyhey123.xiaojieorm.type.SkriptTime
import io.github.heyhey123.xiaojieorm.type.SkriptTimespan
import kotlinx.coroutines.runBlocking
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockbukkit.mockbukkit.MockBukkit
import java.sql.SQLException
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Round-trips the Bukkit- and Skript-backed types through a real MySQL server.
 *
 * `MysqlTypeRoundTripIntegrationTest` covers the types a plain JDBC call can carry. These are the
 * ones whose storage form is produced by Bukkit or Skript code, so only a server plus a database can
 * show that what was written is what comes back. The half that needs no database is covered by
 * `JdbcConverterRoundTripTest`.
 *
 * `NBT_COMPOUND` is absent for the same reason as there: NBT-API cannot build a compound without a
 * real server.
 */
class MysqlConverterRoundTripIntegrationTest : MysqlIntegrationTestBase() {

    @BeforeEach
    fun startServer() {
        MockBukkit.mock()
    }

    @AfterEach
    fun stopServer() {
        MockBukkit.unmock()
    }

    @Test
    fun `item stacks round trip through mysql`() = runBlocking<Unit> {
        recreateTable(valuesTable)
        val stack = ItemStack(Material.STONE, 3)

        queries.insertOne(values(id = 1, item = stack)).execute(valuesTable)

        assertEquals(stack, singleRow().item)
    }

    @Test
    fun `locations round trip through mysql`() = runBlocking<Unit> {
        recreateTable(valuesTable)
        val place = Location(null, 1.5, -2.5, 3.5, 90f, -45f)

        queries.insertOne(values(id = 1, place = place)).execute(valuesTable)

        assertEquals(place, singleRow().place)
    }

    @Test
    fun `skript times and timespans round trip through mysql`() = runBlocking<Unit> {
        recreateTable(valuesTable)

        queries.insertOne(
            values(id = 1, tick = SkriptTime(1234), span = SkriptTimespan(5000L))
        ).execute(valuesTable)

        val row = singleRow()
        assertEquals(SkriptTime(1234), row.tick)
        assertEquals(SkriptTimespan(5000L), row.span)
    }

    @Test
    fun `a date keeps its calendar day and drops the time of day`() = runBlocking<Unit> {
        recreateTable(valuesTable)
        val zone = ZoneId.systemDefault()
        val day = LocalDate.of(2024, 2, 29)
        val midnight = day.atStartOfDay(zone).toInstant().toEpochMilli()
        val withTime = midnight + Duration.ofHours(13).plusMinutes(45).toMillis()

        queries.insertMany(
            listOf(
                values(id = 1, moment = SkriptDate(midnight)),
                values(id = 2, moment = SkriptDate(withTime))
            )
        ).execute(valuesTable)

        val rows = queries.selectMany(null).execute(valuesTable).readRows().sortedBy { it.id }
        rows.forEach { row ->
            assertEquals(
                day,
                Instant.ofEpochMilli(requireNotNull(row.moment).time).atZone(zone).toLocalDate(),
                "a DATE column keeps the calendar day"
            )
        }
        assertEquals(midnight, rows[1].moment?.time, "a DATE column has no time of day to keep")
    }

    @Test
    fun `a not a number ratio is either kept or rejected, never silently zeroed`() = runBlocking<Unit> {
        recreateTable(valuesTable)

        val outcome = runCatching {
            queries.insertOne(values(id = 1, ratio = Double.NaN)).execute(valuesTable)
            singleRow().ratio
        }

        // MySQL versions disagree about NaN, and so do drivers. Whichever way this server behaves,
        // the value must not come back as a plausible number, which is the failure nobody notices.
        outcome.fold(
            onSuccess = { stored -> assertTrue(stored != null && stored.isNaN(), "NaN came back as $stored") },
            onFailure = { error -> assertIs<SQLException>(error) }
        )
    }

    @Test
    fun `an empty item blob is reported instead of deserialized into nonsense`() = runBlocking<Unit> {
        recreateTable(valuesTable)
        queries.insertOne(values(id = 1)).execute(valuesTable)
        executeSql(
            "UPDATE ${MysqlJdbcDialect.quoteIdentifier(valuesTable.name)} " +
                "SET ${MysqlJdbcDialect.quoteIdentifier("item")} = '' " +
                "WHERE ${MysqlJdbcDialect.quoteIdentifier("id")} = 1"
        )

        val thrown = assertFailsWith<IllegalArgumentException> {
            queries.selectById(1).execute(valuesTable).readRows()
        }

        assertTrue(
            thrown.message.orEmpty().contains("ItemStack"),
            "unexpected message: ${thrown.message}"
        )
    }

    private val valuesTable = Table(
        "converted_values",
        listOf(
            Column("id", IntJdbcDataType(), isPrimaryKey = true, isNullable = false),
            Column("item", ItemStackJdbcDataType()),
            Column("place", LocationJdbcDataType()),
            Column("moment", SkriptDateJdbcDataType()),
            Column("tick", SkriptTimeJdbcDataType()),
            Column("span", SkriptTimespanJdbcDataType()),
            Column("ratio", DoubleJdbcDataType())
        )
    )

    private fun values(
        id: Int,
        item: ItemStack? = null,
        place: Location? = null,
        moment: SkriptDate? = null,
        tick: SkriptTime? = null,
        span: SkriptTimespan? = null,
        ratio: Double? = null
    ): Map<String, Any?> = linkedMapOf(
        "id" to id,
        "item" to item,
        "place" to place,
        "moment" to moment,
        "tick" to tick,
        "span" to span,
        "ratio" to ratio
    )

    private suspend fun singleRow(): ConvertedRow =
        queries.selectById(1).execute(valuesTable).readRows().single()

    private fun CursorResult.readRows(): List<ConvertedRow> = cursor.use { open ->
        buildList { while (open.next()) add(open.readRow()) }
    }

    private fun DataCursor.readRow(): ConvertedRow = ConvertedRow(
        id = requireNotNull(get("id", IntJdbcDataType())),
        item = get("item", ItemStackJdbcDataType()),
        place = get("place", LocationJdbcDataType()),
        moment = get("moment", SkriptDateJdbcDataType()),
        tick = get("tick", SkriptTimeJdbcDataType()),
        span = get("span", SkriptTimespanJdbcDataType()),
        ratio = get("ratio", DoubleJdbcDataType())
    )

    private data class ConvertedRow(
        val id: Int,
        val item: ItemStack?,
        val place: Location?,
        val moment: SkriptDate?,
        val tick: SkriptTime?,
        val span: SkriptTimespan?,
        val ratio: Double?
    )
}
