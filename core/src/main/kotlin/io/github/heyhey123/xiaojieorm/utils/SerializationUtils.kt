@file:Suppress("DEPRECATION")

package io.github.heyhey123.xiaojieorm.utils

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
}
