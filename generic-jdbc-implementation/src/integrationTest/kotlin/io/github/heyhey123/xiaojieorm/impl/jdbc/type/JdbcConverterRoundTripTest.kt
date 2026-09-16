package io.github.heyhey123.xiaojieorm.impl.jdbc.type

import io.github.heyhey123.xiaojieorm.type.SkriptDate
import io.github.heyhey123.xiaojieorm.type.SkriptTime
import io.github.heyhey123.xiaojieorm.type.SkriptTimespan
import io.github.heyhey123.xiaojieorm.type.TypeId
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import java.sql.Blob
import java.sql.SQLException
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Round-trips every supported type through its own converter, in a runtime that has a Bukkit server
 * but no database.
 *
 * This is the half of the round trip that neither a mocked `PreparedStatement` nor a database is
 * needed for: the converter has to turn a domain value into storage form and back without losing
 * anything, and the Bukkit-backed types only get that far once a server can serialize them. The
 * database leg is covered by `MysqlConverterRoundTripIntegrationTest`.
 *
 * `NBT_COMPOUND` has no sample. SkBee is the only NBT implementation the plugin supports, and it is
 * not on this classpath, so nothing here can build a compound to round-trip.
 */
class JdbcConverterRoundTripTest {

    @BeforeEach
    fun startServer() {
        MockBukkit.mock()
    }

    @AfterEach
    fun stopServer() {
        MockBukkit.unmock()
    }

    @Test
    fun `every registered type except nbt has a round trip sample`() {
        assertEquals(
            TypeId.entries.toSet() - TypeId.NBT_COMPOUND,
            samples().keys,
            "a new data type needs a sample here, or a documented reason for not having one"
        )
    }

    @Test
    fun `every sample survives its converter`() {
        samples().forEach { (id, value) ->
            val type = assertIs<JdbcDataType<*>>(JdbcDataTypes.typesRegistry.getValue(id))

            assertEquals(value, roundTrip(type, value), "$id must survive the converter round trip")
        }
    }

    @Test
    fun `extreme scalar values survive their converters`() {
        assertRoundTrip(BooleanJdbcDataType(), false)
        assertRoundTrip(TinyIntJdbcDataType(), Byte.MIN_VALUE)
        assertRoundTrip(TinyIntJdbcDataType(), Byte.MAX_VALUE)
        assertRoundTrip(IntJdbcDataType(), Int.MIN_VALUE)
        assertRoundTrip(IntJdbcDataType(), Int.MAX_VALUE)
        assertRoundTrip(BigIntJdbcDataType(), Long.MIN_VALUE)
        assertRoundTrip(BigIntJdbcDataType(), Long.MAX_VALUE)
        assertRoundTrip(StringJdbcDataType(), "")
        assertRoundTrip(StringJdbcDataType(), "数据库 🚀 ünïcödé")
    }

    @Test
    fun `uuids are stored as exactly sixteen bytes`() {
        val type = UuidJdbcDataType()
        val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

        val storage = assertIs<ByteArray>(storageOf(type, uuid))

        assertEquals(16, storage.size)
        assertEquals(uuid, restore(type, storage))
    }

    @Test
    fun `item stacks are stored as a blob that is freed once it is read`() {
        val type = ItemStackJdbcDataType()
        val stack = ItemStack(Material.STONE, 3)

        val storage = assertIs<Blob>(storageOf(type, stack))
        val restored = assertIs<ItemStack>(restore(type, storage))

        assertEquals(stack, restored)
        // Reading consumes the blob, so the caller never has to free what a read produced.
        assertFailsWith<SQLException> { storage.length() }
    }

    @Test
    fun `locations round trip with their orientation`() {
        val type = LocationJdbcDataType()
        val location = Location(null, 1.5, -2.5, 3.5, 90f, -45f)

        assertEquals(location, restore(type, storageOf(type, location)))
    }

    @Test
    fun `configuration serializables round trip`() {
        val type = ConfigurationSerializableJdbcDataType()
        val stack = ItemStack(Material.DIAMOND, 7)

        assertEquals(stack, restore(type, storageOf(type, stack)))
    }

    @Test
    fun `a serialized location fits the column its type declares`() {
        // The declared default size is what a column gets when the author does not give one, so a
        // value that does not fit it is a value the type cannot store: MySQL reports "Data too long
        // for column" and, outside strict mode, would truncate it silently.
        val type = LocationJdbcDataType()
        val storage = assertIs<ByteArray>(storageOf(type, Location(null, 1.5, -2.5, 3.5, 90f, -45f)))

        assertTrue(
            storage.size <= type.defaultSize,
            "a serialized location is ${storage.size} bytes but the type declares a column of ${type.defaultSize}"
        )
    }

    @Test
    fun `skript temporal values round trip`() {
        assertRoundTrip(SkriptDateJdbcDataType(), SkriptDate(0L))
        assertRoundTrip(SkriptTimeJdbcDataType(), SkriptTime(1234))
        assertRoundTrip(SkriptTimespanJdbcDataType(), SkriptTimespan(5000L))
    }

    @Test
    fun `a negative stored timespan is rejected by the domain type`() {
        // Skript's Timespan cannot represent a negative duration, so a BIGINT that something else
        // wrote fails loudly here instead of quietly becoming a wrong value.
        assertFailsWith<IllegalArgumentException> {
            restore(SkriptTimespanJdbcDataType(), -5000L)
        }
    }

    /** One representative domain value per type, except the one that cannot be built here. */
    private fun samples(): Map<TypeId, Any> = mapOf(
        TypeId.BOOLEAN to true,
        TypeId.TINYINT to Byte.MIN_VALUE,
        TypeId.INT to Int.MIN_VALUE,
        TypeId.BIGINT to Long.MAX_VALUE,
        TypeId.DOUBLE to -1.5,
        TypeId.FLOAT to 2.25f,
        TypeId.STRING to "数据库 🚀",
        TypeId.UUID to UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
        TypeId.ITEMSTACK to ItemStack(Material.STONE),
        TypeId.LOCATION to Location(null, 1.5, -2.5, 3.5),
        TypeId.BUKKIT_SERIALIZABLE to ItemStack(Material.DIRT),
        TypeId.DATE to SkriptDate(0L),
        TypeId.TIME to SkriptTime(1234),
        TypeId.TIMESPAN to SkriptTimespan(5000L)
    )

    private fun <T : Any> assertRoundTrip(type: JdbcDataType<T>, value: T) {
        assertEquals(value, restore(type, storageOf(type, value)), "${type.typeCode} lost its value")
    }

    private fun <T : Any> storageOf(type: JdbcDataType<T>, value: T): Any = converterOf(type).toStorage(value)

    private fun <T : Any> restore(type: JdbcDataType<T>, storage: Any): T = converterOf(type).fromStorage(storage)

    private fun roundTrip(type: JdbcDataType<*>, value: Any): Any = restoreRaw(type, storageOfRaw(type, value))

    private fun storageOfRaw(type: JdbcDataType<*>, value: Any): Any = converterOfRaw(type).toStorage(value)

    private fun restoreRaw(type: JdbcDataType<*>, storage: Any): Any = converterOfRaw(type).fromStorage(storage)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> converterOf(type: JdbcDataType<T>): ValueConverter<T, Any> =
        type.converter as ValueConverter<T, Any>

    @Suppress("UNCHECKED_CAST")
    private fun converterOfRaw(type: JdbcDataType<*>): ValueConverter<Any, Any> =
        type.converter as ValueConverter<Any, Any>
}
