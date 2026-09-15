package io.github.heyhey123.xiaojieorm.impl.mongo.type

import de.tr7zw.nbtapi.NBTCompound
import io.github.heyhey123.xiaojieorm.type.SkriptDate
import io.github.heyhey123.xiaojieorm.type.SkriptTime
import io.github.heyhey123.xiaojieorm.type.SkriptTimespan
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import io.github.heyhey123.xiaojieorm.utils.SerializationUtils
import org.bson.types.Binary
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.util.UUID

object TinyIntMongoValueConverter : ValueConverter<Byte, Int>(
    Byte::class.java,
    Int::class.java
) {

    override fun toStorage(value: Byte): Int = value.toInt()

    override fun fromStorage(value: Int): Byte = value.toByte()
}

object FloatMongoValueConverter : ValueConverter<Float, Double>(
    Float::class.java,
    Double::class.java
) {

    override fun toStorage(value: Float): Double = value.toDouble()

    override fun fromStorage(value: Double): Float = value.toFloat()
}

object UuidMongoConverter : ValueConverter<UUID, Binary>(
    UUID::class.java,
    Binary::class.java
) {

    override fun toStorage(value: UUID): Binary {
        val bytes = ByteArray(16)
        val buffer = ByteBuffer.wrap(bytes).apply {
            putLong(value.mostSignificantBits)
            putLong(value.leastSignificantBits)
        }
        return Binary(buffer.array())
    }

    override fun fromStorage(value: Binary): UUID {
        val buffer = ByteBuffer.wrap(value.data)
        val msb = buffer.long
        val lsb = buffer.long
        return UUID(msb, lsb)
    }
}

object ItemStackMongoConverter : ValueConverter<ItemStack, Binary>(
    ItemStack::class.java,
    Binary::class.java
) {

    override fun toStorage(value: ItemStack): Binary {
        val bytes = value.serializeAsBytes()
        return Binary(bytes)
    }

    override fun fromStorage(value: Binary): ItemStack = ItemStack.deserializeBytes(value.data)
}

object LocationMongoConverter : ValueConverter<Location, Binary>(
    Location::class.java,
    Binary::class.java
) {

    override fun toStorage(value: Location): Binary {
        val outputStream = SerializationUtils.BukkitSerialization.serialize(value)
        outputStream.use {
            return Binary(outputStream.toByteArray())
        }
    }

    override fun fromStorage(value: Binary): Location {
        val inputStream = ByteArrayInputStream(value.data)
        inputStream.use {
            return SerializationUtils.BukkitSerialization.deserialize(it) as Location
        }
    }
}

object ConfigurationSerializableMongoConverter : ValueConverter<ConfigurationSerializable, Binary>(
    ConfigurationSerializable::class.java,
    Binary::class.java
) {

    override fun toStorage(value: ConfigurationSerializable): Binary {
        val outputStream = SerializationUtils.BukkitSerialization.serialize(value)
        outputStream.use {
            return Binary(outputStream.toByteArray())
        }
    }

    override fun fromStorage(value: Binary): ConfigurationSerializable {
        val inputStream = ByteArrayInputStream(value.data)
        inputStream.use {
            return SerializationUtils.BukkitSerialization.deserialize(it) as ConfigurationSerializable
        }
    }
}

object NbtMongoConverter : ValueConverter<NBTCompound, Binary>(
    NBTCompound::class.java,
    Binary::class.java
) {

    override fun toStorage(value: NBTCompound): Binary {
        val outputStream = SerializationUtils.NbtSerialization.serialize(value)
        outputStream.use {
            return Binary(outputStream.toByteArray())
        }
    }

    override fun fromStorage(value: Binary): NBTCompound {
        val inputStream = ByteArrayInputStream(value.data)
        inputStream.use {
            return SerializationUtils.NbtSerialization.deserialize(it)
        }
    }
}

object SkriptDateMongoConverter : ValueConverter<SkriptDate, Long>(
    SkriptDate::class.java,
    Long::class.java
) {

    override fun toStorage(value: SkriptDate): Long = value.time

    override fun fromStorage(value: Long): SkriptDate = SkriptDate(value)
}

object SkriptTimeMongoConverter : ValueConverter<SkriptTime, Int>(
    SkriptTime::class.java,
    Int::class.java
) {

    override fun toStorage(value: SkriptTime): Int = value.ticks

    override fun fromStorage(value: Int): SkriptTime = SkriptTime(value)
}

object SkriptTimespanMongoConverter : ValueConverter<SkriptTimespan, Long>(
    SkriptTimespan::class.java,
    Long::class.java
) {

    override fun toStorage(value: SkriptTimespan): Long = value.duration.toMillis()

    override fun fromStorage(value: Long): SkriptTimespan = SkriptTimespan(value)
}
