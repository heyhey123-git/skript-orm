package io.github.heyhey123.skriptorm.impl.mongo.type

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

    override val converter: ValueConverter<Any, Binary> = NbtMongoConverter
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
