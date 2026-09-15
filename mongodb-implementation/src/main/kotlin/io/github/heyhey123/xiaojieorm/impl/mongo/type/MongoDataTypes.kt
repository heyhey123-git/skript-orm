package io.github.heyhey123.xiaojieorm.impl.mongo.type

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
import org.bson.types.Binary
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import java.util.UUID

object MongoDataTypes : DataTypes() {

    override val typesRegistry: MutableMap<TypeId, DataType<*>> = mutableMapOf(
        TypeId.BOOLEAN to BooleanDataType(),
        TypeId.TINYINT to TinyIntMongoDataType,
        TypeId.INT to IntDataType(),
        TypeId.BIGINT to BigIntDataType(),
        TypeId.DOUBLE to DoubleDataType(),
        TypeId.FLOAT to FloatMongoDataType,
        TypeId.STRING to StringDataType(),
        TypeId.UUID to UuidMongoDataType,
        TypeId.ITEMSTACK to ItemStackMongoDataType,
        TypeId.LOCATION to LocationMongoDataType,
        TypeId.BUKKIT_SERIALIZABLE to ConfigurationSerializableMongoDataType,
        TypeId.NBT_COMPOUND to NbtMongoDataType,
        TypeId.DATE to SkriptDateMongoDataType,
        TypeId.TIME to SkriptTimeMongoDataType,
        TypeId.TIMESPAN to SkriptTimespanMongoDataType
    )
}

object TinyIntMongoDataType : TinyIntDataType() {

    override val converter: ValueConverter<Byte, Int> = TinyIntMongoValueConverter
}

object FloatMongoDataType : FloatDataType() {

    override val converter: ValueConverter<Float, Double> = FloatMongoValueConverter
}

object UuidMongoDataType : UuidDataType() {

    override val converter: ValueConverter<UUID, Binary> = UuidMongoConverter
}

object ItemStackMongoDataType : ItemStackDataType() {

    override val converter: ValueConverter<ItemStack, Binary> = ItemStackMongoConverter
}

object LocationMongoDataType : LocationDataType() {

    override val converter: ValueConverter<Location, Binary> = LocationMongoConverter
}

object ConfigurationSerializableMongoDataType : ConfigurationSerializableDataType() {

    override val converter: ValueConverter<ConfigurationSerializable, Binary> = ConfigurationSerializableMongoConverter
}

object NbtMongoDataType : NbtDataType() {

    override val converter: ValueConverter<NBTCompound, Binary> = NbtMongoConverter
}

object SkriptDateMongoDataType : SkriptDateDataType() {

    override val converter: ValueConverter<SkriptDate, Long> = SkriptDateMongoConverter
}

object SkriptTimeMongoDataType : SkriptTimeDataType() {

    override val converter: ValueConverter<SkriptTime, Int> = SkriptTimeMongoConverter
}

object SkriptTimespanMongoDataType : SkriptTimespanDataType() {

    override val converter: ValueConverter<SkriptTimespan, Long> = SkriptTimespanMongoConverter
}
