package io.github.heyhey123.xiaojieorm.impl.rocksdb.type

import de.tr7zw.nbtapi.NBTCompound
import io.github.heyhey123.xiaojieorm.type.SkriptDate
import io.github.heyhey123.xiaojieorm.type.SkriptTime
import io.github.heyhey123.xiaojieorm.type.SkriptTimespan
import io.github.heyhey123.xiaojieorm.type.ValueConverter
import io.github.heyhey123.xiaojieorm.utils.SerializationUtils
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.util.*

/**
 * RocksDB value converter base class.
 *
 * @param D Domain type.
 *
 * @param domainType Domain type class.
 */
abstract class RocksValueConverter<D : Any>(domainType: Class<D>) : ValueConverter<D, ByteArray>(domainType, ByteArray::class.java)

object BooleanRocksConverter : RocksValueConverter<Boolean>(Boolean::class.java) {
    override fun toStorage(value: Boolean): ByteArray {
        return byteArrayOf(if (value) 1 else 0)
    }

    override fun fromStorage(value: ByteArray): Boolean {
        return value.isNotEmpty() && value[0].toInt() != 0
    }
}

object TinyIntRocksConverter : RocksValueConverter<Byte>(Byte::class.java) {
    override fun toStorage(value: Byte): ByteArray {
        return byteArrayOf(value)
    }

    override fun fromStorage(value: ByteArray): Byte {
        return if (value.isNotEmpty()) value[0] else 0
    }
}

object IntRocksConverter : RocksValueConverter<Int>(Int::class.java) {
    override fun toStorage(value: Int): ByteArray =
        ByteBuffer.allocate(4).putInt(value).array()

    override fun fromStorage(value: ByteArray) =
        ByteBuffer.wrap(value).int
}

object BigIntRocksConverter : RocksValueConverter<Long>(Long::class.java) {
    override fun toStorage(value: Long): ByteArray =
        ByteBuffer.allocate(8).putLong(value).array()

    override fun fromStorage(value: ByteArray) =
        ByteBuffer.wrap(value).long
}

object DoubleRocksConverter : RocksValueConverter<Double>(Double::class.java) {
    override fun toStorage(value: Double): ByteArray =
        ByteBuffer.allocate(8).putDouble(value).array()

    override fun fromStorage(value: ByteArray) =
        ByteBuffer.wrap(value).double
}

object FloatRocksConverter : RocksValueConverter<Float>(Float::class.java) {
    override fun toStorage(value: Float): ByteArray =
        ByteBuffer.allocate(4).putFloat(value).array()

    override fun fromStorage(value: ByteArray) =
        ByteBuffer.wrap(value).float
}

object StringRocksConverter : RocksValueConverter<String>(String::class.java) {
    override fun toStorage(value: String): ByteArray =
        value.toByteArray(Charsets.UTF_8)

    override fun fromStorage(value: ByteArray): String =
        String(value, Charsets.UTF_8)
}

object UuidRocksConverter : RocksValueConverter<UUID>(UUID::class.java) {
    override fun toStorage(value: UUID): ByteArray {
        val buffer = ByteBuffer.allocate(16)
        buffer.putLong(value.mostSignificantBits)
        buffer.putLong(value.leastSignificantBits)
        return buffer.array()
    }

    override fun fromStorage(value: ByteArray): java.util.UUID {
        val buffer = ByteBuffer.wrap(value)
        val msb = buffer.long
        val lsb = buffer.long
        return UUID(msb, lsb)
    }
}

object ItemStackRocksConverter : RocksValueConverter<ItemStack>(ItemStack::class.java) {

    override fun toStorage(value: ItemStack) = value.serializeAsBytes()

    override fun fromStorage(value: ByteArray) = ItemStack.deserializeBytes(value)
}

object LocationRocksConverter : RocksValueConverter<Location>(Location::class.java) {
    override fun toStorage(value: Location): ByteArray {
        val outputStream = SerializationUtils.BukkitSerialization.serialize(value)
        outputStream.use {
            return outputStream.toByteArray()
        }
    }

    override fun fromStorage(value: ByteArray): Location {
        val inputStream = ByteArrayInputStream(value)
        inputStream.use {
            val obj = SerializationUtils.BukkitSerialization.deserialize(inputStream)
            return obj as Location
        }
    }
}

object ConfigurationSerializableRocksConverter :
    RocksValueConverter<ConfigurationSerializable>(
        ConfigurationSerializable::class.java
    ) {
    override fun toStorage(value: ConfigurationSerializable): ByteArray {
        val outputStream = SerializationUtils.BukkitSerialization.serialize(value)
        outputStream.use {
            return outputStream.toByteArray()
        }
    }

    override fun fromStorage(value: ByteArray): ConfigurationSerializable {
        val inputStream = ByteArrayInputStream(value)
        inputStream.use {
            val obj = SerializationUtils.BukkitSerialization.deserialize(inputStream)
            return obj as ConfigurationSerializable
        }
    }
}

object NbtRocksConverter : ValueConverter<NBTCompound, ByteArray>(
    NBTCompound::class.java, ByteArray::class.java
) {
    override fun toStorage(value: NBTCompound): ByteArray =
        SerializationUtils.NbtSerialization
            .serialize(value)
            .use { it.toByteArray() }

    override fun fromStorage(value: ByteArray): NBTCompound =
        ByteArrayInputStream(value).use {
            SerializationUtils.NbtSerialization.deserialize(it)
        }
}

object SkriptDateRocksConverter : ValueConverter<SkriptDate, ByteArray>(
    SkriptDate::class.java, ByteArray::class.java
) {
    override fun toStorage(value: SkriptDate) =
        BigIntRocksConverter.toStorage(value.time)

    override fun fromStorage(value: ByteArray) =
        SkriptDate(BigIntRocksConverter.fromStorage(value))
}

object SkriptTimeRocksConverter : ValueConverter<SkriptTime, ByteArray>(
    SkriptTime::class.java, ByteArray::class.java
) {
    override fun toStorage(value: SkriptTime) =
        IntRocksConverter.toStorage(value.ticks)

    override fun fromStorage(value: ByteArray) =
        SkriptTime(IntRocksConverter.fromStorage(value))
}

object SkriptTimespanRocksConverter : ValueConverter<SkriptTimespan, ByteArray>(
    SkriptTimespan::class.java, ByteArray::class.java
) {
    override fun toStorage(value: SkriptTimespan) =
        BigIntRocksConverter.toStorage(value.duration.toMillis())

    override fun fromStorage(value: ByteArray) =
        SkriptTimespan(BigIntRocksConverter.fromStorage(value))
}
