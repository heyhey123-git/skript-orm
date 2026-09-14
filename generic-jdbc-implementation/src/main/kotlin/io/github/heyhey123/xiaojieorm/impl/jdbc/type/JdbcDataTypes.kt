package io.github.heyhey123.xiaojieorm.impl.jdbc.type

import de.tr7zw.nbtapi.NBTCompound
import io.github.heyhey123.xiaojieorm.type.BigIntDataType
import io.github.heyhey123.xiaojieorm.type.BooleanDataType
import io.github.heyhey123.xiaojieorm.type.ConfigurationSerializableDataType
import io.github.heyhey123.xiaojieorm.type.DataType
import io.github.heyhey123.xiaojieorm.type.DataTypes
import io.github.heyhey123.xiaojieorm.type.DoubleDataType
import io.github.heyhey123.xiaojieorm.type.FloatDataType
import io.github.heyhey123.xiaojieorm.type.IntDataType
import io.github.heyhey123.xiaojieorm.type.ItemStackDataType
import io.github.heyhey123.xiaojieorm.type.LocationDataType
import io.github.heyhey123.xiaojieorm.type.NbtDataType
import io.github.heyhey123.xiaojieorm.type.SkriptDate
import io.github.heyhey123.xiaojieorm.type.SkriptDateDataType
import io.github.heyhey123.xiaojieorm.type.SkriptTime
import io.github.heyhey123.xiaojieorm.type.SkriptTimeDataType
import io.github.heyhey123.xiaojieorm.type.SkriptTimespan
import io.github.heyhey123.xiaojieorm.type.SkriptTimespanDataType
import io.github.heyhey123.xiaojieorm.type.StringDataType
import io.github.heyhey123.xiaojieorm.type.TinyIntDataType
import io.github.heyhey123.xiaojieorm.type.TypeId
import io.github.heyhey123.xiaojieorm.type.UuidDataType
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import java.sql.Blob
import java.sql.Date
import java.sql.JDBCType
import java.util.*

/**
 * Jdbc data types registry.
 *
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
}

open class UuidJdbcDataType : UuidDataType(), JdbcDataType<UUID> {
    override val jdbcType: JDBCType = JDBCType.BINARY
    override val storageName: String = "BINARY"
    override val converter: ValueConverter<UUID, ByteArray> = UuidJdbcConverter
    override val defaultSize: Int = 16
    override val supportsSize: Boolean = true
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
    override val defaultSize: Int = 255
    override val supportsSize: Boolean = true
}

open class ConfigurationSerializableJdbcDataType
    : ConfigurationSerializableDataType(), JdbcDataType<ConfigurationSerializable> {
    override val jdbcType: JDBCType = JDBCType.BLOB
    override val storageName: String = "BLOB"
    override val converter: ValueConverter<ConfigurationSerializable, Blob> = ConfigurationSerializableJdbcConverter
}

open class NbtJdbcDataType : NbtDataType(), JdbcDataType<NBTCompound> {
    override val jdbcType: JDBCType = JDBCType.BLOB
    override val storageName: String = "BLOB"
    override val converter: ValueConverter<NBTCompound, Blob> = NbtJdbcConverter
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
