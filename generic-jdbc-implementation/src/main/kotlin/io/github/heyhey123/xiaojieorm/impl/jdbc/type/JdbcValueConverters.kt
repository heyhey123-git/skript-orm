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
import java.nio.ByteBuffer
import java.sql.Blob
import java.sql.Date
import java.util.UUID
import javax.sql.rowset.serial.SerialBlob

/**
 * Reads all bytes from this BLOB, applies [operation], and always frees the BLOB.
 * A free failure is suppressed onto an earlier failure or thrown when it is the only failure.
 */
private inline fun <T> Blob.consumeBytes(operation: (ByteArray) -> T): T {
    var failure: Throwable? = null
    try {
        return binaryStream.use { operation(it.readBytes()) }
    } catch (error: Throwable) {
        failure = error
        throw error
    } finally {
        try {
            free()
        } catch (freeError: Throwable) {
            if (failure != null) failure.addSuppressed(freeError) else throw freeError
        }
    }
}

/** Stores UUIDs as 16 big-endian bytes: most-significant bits followed by least-significant bits. */
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
        require(value.size == 16) { "A stored UUID must contain exactly 16 bytes, but contained ${value.size}." }
        val buffer = ByteBuffer.wrap(value)
        return UUID(buffer.long, buffer.long)
    }
}

/** Stores ItemStacks as Bukkit's binary item format in a statement-owned BLOB. */
object ItemStackJdbcConverter : ValueConverter<ItemStack, Blob>(
    ItemStack::class.java, Blob::class.java
) {
    override fun toStorage(value: ItemStack): Blob = SerialBlob(value.serializeAsBytes())

    override fun fromStorage(value: Blob): ItemStack = value.consumeBytes { bytes ->
        try {
            ItemStack.deserializeBytes(bytes)
        } catch (error: Throwable) {
            throw IllegalArgumentException("Failed to deserialize an ItemStack from JDBC BLOB data.", error)
        }
    }
}

/** Stores Bukkit Locations as Bukkit object-serialization bytes. */
object LocationJdbcConverter : ValueConverter<Location, ByteArray>(
    Location::class.java, ByteArray::class.java
) {
    override fun toStorage(value: Location): ByteArray =
        SerializationUtils.BukkitSerialization.serialize(value).use { it.toByteArray() }

    override fun fromStorage(value: ByteArray): Location = ByteArrayInputStream(value).use { input ->
        val deserialized = SerializationUtils.BukkitSerialization.deserialize(input)
        deserialized as? Location
            ?: throw IllegalArgumentException(
                "Expected a serialized Bukkit Location, but found ${deserialized::class.java.name}."
            )
    }
}

object ConfigurationSerializableJdbcConverter :
    ValueConverter<ConfigurationSerializable, Blob>(
        ConfigurationSerializable::class.java,
        Blob::class.java
    ) {
    override fun toStorage(value: ConfigurationSerializable): Blob =
        SerializationUtils.BukkitSerialization.serialize(value).use { SerialBlob(it.toByteArray()) }

    override fun fromStorage(value: Blob): ConfigurationSerializable = value.consumeBytes { bytes ->
        ByteArrayInputStream(bytes).use { input ->
            val deserialized = SerializationUtils.BukkitSerialization.deserialize(input)
            deserialized as? ConfigurationSerializable
                ?: throw IllegalArgumentException(
                    "Expected a Bukkit ConfigurationSerializable, but found ${deserialized::class.java.name}."
                )
        }
    }
}

object NbtJdbcConverter : ValueConverter<NBTCompound, Blob>(
    NBTCompound::class.java, Blob::class.java
) {
    override fun toStorage(value: NBTCompound): Blob =
        SerializationUtils.NbtSerialization.serialize(value).use { SerialBlob(it.toByteArray()) }

    override fun fromStorage(value: Blob): NBTCompound = value.consumeBytes { bytes ->
        ByteArrayInputStream(bytes).use { input ->
            try {
                SerializationUtils.NbtSerialization.deserialize(input)
            } catch (error: Throwable) {
                throw IllegalArgumentException("Failed to deserialize NBT from JDBC BLOB data.", error)
            }
        }
    }
}

/** Stores Skript dates as [Date] values. */
object SkriptDateJdbcConverter : ValueConverter<SkriptDate, Date>(
    SkriptDate::class.java, Date::class.java
) {
    override fun toStorage(value: SkriptDate): Date = Date(value.time)
    override fun fromStorage(value: Date): SkriptDate = SkriptDate(value.time)
}

/** Stores Skript times as their integer tick count. */
object SkriptTimeJdbcConverter : ValueConverter<SkriptTime, Int>(
    SkriptTime::class.java, Integer.TYPE
) {
    override fun toStorage(value: SkriptTime): Int = value.ticks
    override fun fromStorage(value: Int): SkriptTime = SkriptTime(value)
}

/** Stores Skript timespans as milliseconds. */
object SkriptTimespanJdbcConverter : ValueConverter<SkriptTimespan, Long>(
    SkriptTimespan::class.java, Long::class.java
) {
    override fun toStorage(value: SkriptTimespan): Long = value.duration.toMillis()
    override fun fromStorage(value: Long): SkriptTimespan = SkriptTimespan(value)
}
