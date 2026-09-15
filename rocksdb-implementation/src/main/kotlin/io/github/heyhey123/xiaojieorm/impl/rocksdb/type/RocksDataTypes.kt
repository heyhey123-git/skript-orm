package io.github.heyhey123.xiaojieorm.impl.rocksdb.type

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
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import java.util.UUID

object RocksDataTypes : DataTypes() {

    override val typesRegistry: MutableMap<TypeId, DataType<*>> = mutableMapOf(
        TypeId.BOOLEAN to BooleanRocksDataType,
        TypeId.TINYINT to TinyIntRocksDataType,
        TypeId.INT to IntRocksDataType,
        TypeId.BIGINT to BigIntRocksDataType,
        TypeId.DOUBLE to DoubleRocksDataType,
        TypeId.FLOAT to FloatRocksDataType,
        TypeId.STRING to StringRocksDataType,
        TypeId.UUID to UuidRocksDataType,
        TypeId.ITEMSTACK to ItemStackRocksDataType,
        TypeId.LOCATION to LocationRocksDataType,
        TypeId.BUKKIT_SERIALIZABLE to ConfigurationSerializableRocksDataType,
        TypeId.NBT_COMPOUND to NbtRocksDataType,
        TypeId.DATE to SkriptDateRocksDataType,
        TypeId.TIME to SkriptTimeRocksDataType,
        TypeId.TIMESPAN to SkriptTimespanRocksDataType
    )
}

object BooleanRocksDataType : BooleanDataType(), RocksDataType<Boolean> {

    override val converter: RocksValueConverter<Boolean> = BooleanRocksConverter
}

object TinyIntRocksDataType : TinyIntDataType(), RocksDataType<Byte> {

    override val converter: RocksValueConverter<Byte> = TinyIntRocksConverter
}

object IntRocksDataType : IntDataType(), RocksDataType<Int> {

    override val converter: RocksValueConverter<Int> = IntRocksConverter
}

object BigIntRocksDataType : BigIntDataType(), RocksDataType<Long> {

    override val converter: RocksValueConverter<Long> = BigIntRocksConverter
}

object DoubleRocksDataType : DoubleDataType(), RocksDataType<Double> {

    override val converter: RocksValueConverter<Double> = DoubleRocksConverter
}

object FloatRocksDataType : FloatDataType(), RocksDataType<Float> {

    override val converter: RocksValueConverter<Float> = FloatRocksConverter
}

object StringRocksDataType : StringDataType(), RocksDataType<String> {

    override val converter: RocksValueConverter<String> = StringRocksConverter
}

object UuidRocksDataType : UuidDataType(), RocksDataType<UUID> {

    override val converter: RocksValueConverter<UUID> = UuidRocksConverter
}

object ItemStackRocksDataType : ItemStackDataType(), RocksDataType<ItemStack> {

    override val converter = ItemStackRocksConverter
}

object LocationRocksDataType : LocationDataType(), RocksDataType<Location> {

    override val converter = LocationRocksConverter
}

object ConfigurationSerializableRocksDataType :
    ConfigurationSerializableDataType(), RocksDataType<ConfigurationSerializable> {

    override val converter = ConfigurationSerializableRocksConverter
}

object NbtRocksDataType : NbtDataType(), RocksDataType<NBTCompound> {

    override val converter = NbtRocksConverter
}

object SkriptDateRocksDataType : SkriptDateDataType(), RocksDataType<SkriptDate> {

    override val converter = SkriptDateRocksConverter
}

object SkriptTimeRocksDataType : SkriptTimeDataType(), RocksDataType<SkriptTime> {

    override val converter = SkriptTimeRocksConverter
}

object SkriptTimespanRocksDataType : SkriptTimespanDataType(), RocksDataType<SkriptTimespan> {

    override val converter = SkriptTimespanRocksConverter
}
