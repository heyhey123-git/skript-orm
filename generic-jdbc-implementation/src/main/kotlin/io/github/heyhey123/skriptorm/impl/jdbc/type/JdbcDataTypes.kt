package io.github.heyhey123.skriptorm.impl.jdbc.type

import io.github.heyhey123.skriptorm.type.BigIntDataType
import io.github.heyhey123.skriptorm.type.BooleanDataType
import io.github.heyhey123.skriptorm.type.ConfigurationSerializableDataType
import io.github.heyhey123.skriptorm.type.DataType
import io.github.heyhey123.skriptorm.type.DataTypes
import io.github.heyhey123.skriptorm.type.DoubleDataType
import io.github.heyhey123.skriptorm.type.FloatDataType
import io.github.heyhey123.skriptorm.type.IntDataType
import io.github.heyhey123.skriptorm.type.ItemStackDataType
import io.github.heyhey123.skriptorm.type.LocationDataType
import io.github.heyhey123.skriptorm.type.NbtDataType
import io.github.heyhey123.skriptorm.type.SkriptDate
import io.github.heyhey123.skriptorm.type.SkriptDateDataType
import io.github.heyhey123.skriptorm.type.SkriptTime
import io.github.heyhey123.skriptorm.type.SkriptTimeDataType
import io.github.heyhey123.skriptorm.type.SkriptTimespan
import io.github.heyhey123.skriptorm.type.SkriptTimespanDataType
import io.github.heyhey123.skriptorm.type.StringDataType
import io.github.heyhey123.skriptorm.type.TinyIntDataType
import io.github.heyhey123.skriptorm.type.TypeId
import io.github.heyhey123.skriptorm.type.UuidDataType
import io.github.heyhey123.skriptorm.type.ValueConverter
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import java.sql.Blob
import java.sql.Date
import java.sql.JDBCType
import java.util.UUID

/**
 * JDBC data type registry.
 */
object JdbcDataTypes : DataTypes() {

    override val typesRegistry = mutableMapOf<TypeId, DataType<*>>(
        TypeId.BOOLEAN to BooleanJdbcDataType(),
        TypeId.TINYINT to TinyIntJdbcDataType(),
        TypeId.INT to IntJdbcDataType(),
        TypeId.BIGINT to BigIntJdbcDataType(),
        TypeId.DOUBLE to DoubleJdbcDataType(),
        TypeId.FLOAT to FloatJdbcDataType(),
        TypeId.STRING to StringJdbcDataType(),
        TypeId.UUID to UuidJdbcDataType(),
        TypeId.ITEMSTACK to ItemStackJdbcDataType(),
        TypeId.LOCATION to LocationJdbcDataType(),
        TypeId.BUKKIT_SERIALIZABLE to ConfigurationSerializableJdbcDataType(),
        TypeId.NBT_COMPOUND to NbtJdbcDataType(),
        TypeId.DATE to SkriptDateJdbcDataType(),
        TypeId.TIME to SkriptTimeJdbcDataType(),
        TypeId.TIMESPAN to SkriptTimespanJdbcDataType()
    )
}

open class BooleanJdbcDataType : BooleanDataType(), JdbcDataType<Boolean> {

    override val jdbcType: JDBCType = JDBCType.BOOLEAN
    override val storageName: String = "BOOLEAN"
}

open class TinyIntJdbcDataType : TinyIntDataType(), JdbcDataType<Byte> {

    override val jdbcType: JDBCType = JDBCType.TINYINT
    override val storageName: String = "TINYINT"
}

open class IntJdbcDataType : IntDataType(), JdbcDataType<Int> {

    override val jdbcType: JDBCType = JDBCType.INTEGER
    override val storageName: String = "INT"
}

open class BigIntJdbcDataType : BigIntDataType(), JdbcDataType<Long> {

    override val jdbcType: JDBCType = JDBCType.BIGINT
    override val storageName: String = "BIGINT"
}

open class DoubleJdbcDataType : DoubleDataType(), JdbcDataType<Double> {

    override val jdbcType: JDBCType = JDBCType.DOUBLE
    override val storageName: String = "DOUBLE"
}

open class FloatJdbcDataType : FloatDataType(), JdbcDataType<Float> {

    override val jdbcType: JDBCType = JDBCType.FLOAT
    override val storageName: String = "FLOAT"
}

open class StringJdbcDataType : StringDataType(), JdbcDataType<String> {

    override val jdbcType: JDBCType = JDBCType.VARCHAR
    override val storageName: String = "VARCHAR"
    override val defaultSize: Int = 255
    override val supportsSize: Boolean = true

    /** A `VARCHAR` that holds more than the declaration asks for serves it: a string reads back as written. */
    override fun servesSize(storedSize: Int, declaredSize: Int): Boolean = storedSize >= declaredSize
}

open class UuidJdbcDataType : UuidDataType(), JdbcDataType<UUID> {

    override val jdbcType: JDBCType = JDBCType.BINARY
    override val storageName: String = "BINARY"
    override val converter: ValueConverter<UUID, ByteArray> = UuidJdbcConverter
    override val defaultSize: Int = 16
    override val supportsSize: Boolean = true

    // BINARY is fixed-width. A wider column pads the 16-byte UUID, so size matching must stay exact.
}

open class ItemStackJdbcDataType : ItemStackDataType(), JdbcDataType<ItemStack> {

    override val jdbcType: JDBCType = JDBCType.BLOB
    override val storageName: String = "BLOB"
    override val converter: ValueConverter<ItemStack, Blob> = ItemStackJdbcConverter
}

open class LocationJdbcDataType : LocationDataType(), JdbcDataType<Location> {

    override val jdbcType: JDBCType = JDBCType.BINARY
    override val storageName: String = "VARBINARY"
    override val converter: ValueConverter<Location, ByteArray> = LocationJdbcConverter

    /**
     * Bukkit's serialized location can exceed 255 bytes even without a world. Reserve enough
     * `VARBINARY` space for the location and its world name.
     */
    override val defaultSize: Int = 2048
    override val supportsSize: Boolean = true

    /** `VARBINARY` is a capacity: a column with more of it serves the declaration, with no padding to read. */
    override fun servesSize(storedSize: Int, declaredSize: Int): Boolean = storedSize >= declaredSize
}

open class ConfigurationSerializableJdbcDataType :
    ConfigurationSerializableDataType(), JdbcDataType<ConfigurationSerializable> {

    override val jdbcType: JDBCType = JDBCType.BLOB
    override val storageName: String = "BLOB"
    override val converter: ValueConverter<ConfigurationSerializable, Blob> = ConfigurationSerializableJdbcConverter
}

open class NbtJdbcDataType : NbtDataType(), JdbcDataType<Any> {

    override val jdbcType: JDBCType = JDBCType.BLOB
    override val storageName: String = "BLOB"
    override val converter: ValueConverter<Any, Blob> = NbtJdbcConverter
}

open class SkriptDateJdbcDataType : SkriptDateDataType(), JdbcDataType<SkriptDate> {

    override val jdbcType: JDBCType = JDBCType.DATE
    override val storageName: String = "DATE"
    override val converter: ValueConverter<SkriptDate, Date> = SkriptDateJdbcConverter
}

open class SkriptTimeJdbcDataType : SkriptTimeDataType(), JdbcDataType<SkriptTime> {

    override val jdbcType: JDBCType = JDBCType.INTEGER
    override val storageName: String = "INT"
    override val converter: ValueConverter<SkriptTime, Int> = SkriptTimeJdbcConverter
}

open class SkriptTimespanJdbcDataType : SkriptTimespanDataType(), JdbcDataType<SkriptTimespan> {

    override val jdbcType: JDBCType = JDBCType.BIGINT
    override val storageName: String = "BIGINT"
    override val converter: ValueConverter<SkriptTimespan, Long> = SkriptTimespanJdbcConverter
}
