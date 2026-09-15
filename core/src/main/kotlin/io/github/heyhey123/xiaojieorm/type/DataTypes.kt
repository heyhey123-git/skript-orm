package io.github.heyhey123.xiaojieorm.type

import de.tr7zw.nbtapi.NBTCompound
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import java.util.UUID

typealias SkriptDate = ch.njol.skript.util.Date
typealias SkriptTime = ch.njol.skript.util.Time
typealias SkriptTimespan = ch.njol.skript.util.Timespan

abstract class DataTypes {

    /**
     * The registry of data types.
     */
    abstract val typesRegistry: MutableMap<TypeId, DataType<*>>

    operator fun get(typeCode: String): DataType<*>? = typesRegistry[TypeId.fromCode(typeCode)]

    operator fun get(domainType: Class<*>): DataType<*>? =
        typesRegistry.values.find { it.domainType == domainType }

    operator fun set(typeCode: String, dataType: DataType<*>) {
        val typeId = TypeId.fromCode(typeCode)
        require(typeId != null) {
            "Type code '$typeCode' is not registered in TypeId enum."
        }
        typesRegistry[typeId] = dataType
    }

    operator fun contains(domainType: Class<*>): Boolean =
        typesRegistry.values.any { it.domainType == domainType }
}

open class BooleanDataType : DataType<Boolean> {

    override val domainType: Class<Boolean> = Boolean::class.javaObjectType
    override val typeCode: String = "boolean"
}

open class TinyIntDataType : DataType<Byte> {

    override val domainType: Class<Byte> = Byte::class.javaObjectType
    override val typeCode: String = "tinyint"
}

open class IntDataType : DataType<Int> {

    override val domainType: Class<Int> = Int::class.javaObjectType
    override val typeCode: String = "int"
}

open class BigIntDataType : DataType<Long> {

    override val domainType: Class<Long> = Long::class.javaObjectType
    override val typeCode: String = "bigint"
}

open class DoubleDataType : DataType<Double> {

    override val domainType: Class<Double> = Double::class.javaObjectType
    override val typeCode: String = "double"
}

open class FloatDataType : DataType<Float> {

    override val domainType: Class<Float> = Float::class.javaObjectType
    override val typeCode: String = "float"
}

open class StringDataType : DataType<String> {

    override val domainType: Class<String> = String::class.java
    override val typeCode: String = "string"
}

open class UuidDataType : DataType<UUID> {

    override val domainType: Class<UUID> = UUID::class.java
    override val typeCode: String = "uuid"
}

open class ItemStackDataType : DataType<ItemStack> {

    override val domainType: Class<ItemStack> = ItemStack::class.java
    override val typeCode: String = "itemstack"
}

open class LocationDataType : DataType<Location> {

    override val domainType: Class<Location> = Location::class.java
    override val typeCode: String = "location"
}

open class ConfigurationSerializableDataType : DataType<ConfigurationSerializable> {

    override val domainType: Class<ConfigurationSerializable> = ConfigurationSerializable::class.java
    override val typeCode: String = "bukkitserializable"
}

open class NbtDataType : DataType<NBTCompound> {

    override val domainType: Class<NBTCompound> = NBTCompound::class.java
    override val typeCode: String = "nbtcompound"
}

open class SkriptDateDataType : DataType<SkriptDate> {

    override val domainType: Class<SkriptDate> = SkriptDate::class.java
    override val typeCode: String = "date"
}

open class SkriptTimeDataType : DataType<SkriptTime> {

    override val domainType: Class<SkriptTime> = SkriptTime::class.java
    override val typeCode: String = "time"
}

open class SkriptTimespanDataType : DataType<SkriptTimespan> {

    override val domainType: Class<SkriptTimespan> = SkriptTimespan::class.java
    override val typeCode: String = "timespan"
}
