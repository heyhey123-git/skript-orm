@file:Suppress("DEPRECATION")
package io.github.heyhey123.xiaojieorm.utils

import de.tr7zw.nbtapi.NBTCompound
import de.tr7zw.nbtapi.NBTContainer
import de.tr7zw.nbtapi.NBTReflectionUtil
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Serialization utils for various serialization methods.
 *
 */
object SerializationUtils {

    object BukkitSerialization {
        fun serialize(serializable: ConfigurationSerializable): ByteArrayOutputStream {
            val outputStream = ByteArrayOutputStream()
            BukkitObjectOutputStream(outputStream).use {
                it.writeObject(serializable)
                it.flush()
                return outputStream
            }
        }

        fun deserialize(data: InputStream): Any {
            BukkitObjectInputStream(data).use {
                return it.readObject()
            }
        }
    }

    /**
     * Serialization utils for [NBTCompound].
     *
     */
    object NbtSerialization {
        fun serialize(serializable: NBTCompound): ByteArrayOutputStream {
            val outputStream = ByteArrayOutputStream()
            serializable.writeCompound(outputStream)
            return outputStream
        }

        fun deserialize(data: InputStream): NBTCompound = NBTContainer(NBTReflectionUtil.readNBT(data))
    }
}
