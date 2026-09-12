package io.github.heyhey123.xiaojieorm.impl.jdbc.type

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
import java.io.InputStream
import java.nio.ByteBuffer
import java.sql.Blob
import java.sql.Date
import java.util.*
import javax.sql.rowset.serial.SerialBlob

object UuidJdbcConverter : ValueConverter<UUID, ByteArray>(
    UUID::class.java, ByteArray::class.java
) {
    override fun toStorage(value: UUID): ByteArray {
        val bytes = ByteArray(16)
        val buffer = ByteBuffer.wrap(bytes).apply {
            putLong(value.mostSignificantBits)
            putLong(value.leastSignificantBits)
        }

        return buffer.array()
    }

    override fun fromStorage(value: ByteArray): UUID {
        val buffer = ByteBuffer.wrap(value)
        val msb = buffer.long
        val lsb = buffer.long
        return UUID(msb, lsb)
    }
}

object ItemStackJdbcConverter : ValueConverter<ItemStack, Blob>(
    ItemStack::class.java, Blob::class.java
) {
    override fun toStorage(value: ItemStack): Blob {
        val bytes = value.serializeAsBytes()
        return SerialBlob(bytes)
    }

    override fun fromStorage(value: Blob): ItemStack {
        val inputStream: InputStream = value.binaryStream
        inputStream.use {
            val bytes = it.readBytes()
            return ItemStack.deserializeBytes(bytes)
        }
    }
}

object LocationJdbcConverter : ValueConverter<Location, ByteArray>(
    Location::class.java, ByteArray::class.java
) {
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

object ConfigurationSerializableJdbcConverter :
    ValueConverter<ConfigurationSerializable, Blob>(
        ConfigurationSerializable::class.java,
        Blob::class.java
    ) {
    override fun toStorage(value: ConfigurationSerializable): Blob {
        val outputStream = SerializationUtils.BukkitSerialization.serialize(value)
        outputStream.use {
            val bytes = outputStream.toByteArray()
            return SerialBlob(bytes)
        }
    }

    override fun fromStorage(value: Blob): ConfigurationSerializable {
        val inputStream: InputStream = value.binaryStream
        inputStream.use {
            val obj = SerializationUtils.BukkitSerialization.deserialize(inputStream)
            return obj as ConfigurationSerializable
        }
    }
}

object NbtJdbcConverter : ValueConverter<NBTCompound, Blob>(
    NBTCompound::class.java, Blob::class.java
) {
    override fun toStorage(value: NBTCompound): Blob {
        val outputStream = SerializationUtils.NbtSerialization.serialize(value)
        outputStream.use {
            return SerialBlob(outputStream.toByteArray())
        }
    }

    override fun fromStorage(value: Blob): NBTCompound {
        val inputStream = value.binaryStream
        inputStream.use {
            return SerializationUtils.NbtSerialization.deserialize(inputStream)
        }
    }
}

object SkriptDateJdbcConverter : ValueConverter<SkriptDate, Date>(
    SkriptDate::class.java, Date::class.java
) {
    override fun toStorage(value: SkriptDate): Date {
        return Date(value.time)
    }

    override fun fromStorage(value: Date): SkriptDate {
        return io.github.heyhey123.xiaojieorm.type.SkriptDate(value.time)
    }
}

object SkriptTimeJdbcConverter : ValueConverter<SkriptTime, Int>(
    SkriptTime::class.java, Integer.TYPE
) {
    override fun toStorage(value: SkriptTime): Int = value.ticks

    override fun fromStorage(value: Int): SkriptTime = SkriptTime(value)
}

object SkriptTimespanJdbcConverter : ValueConverter<SkriptTimespan, Long>(
    SkriptTimespan::class.java, Long::class.java
) {
    override fun toStorage(value: SkriptTimespan): Long = value.duration.toMillis()

    override fun fromStorage(value: Long): SkriptTimespan = SkriptTimespan(value)
}
