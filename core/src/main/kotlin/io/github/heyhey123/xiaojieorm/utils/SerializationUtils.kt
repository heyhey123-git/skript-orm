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
//    object YggdrasilSerialization {
//        @Suppress("UNCHECKED_CAST")
//        fun serialize(serializable: Any, typeCode: String): ByteArrayOutputStream {
//            var classInfo = Classes.getClassInfo(typeCode)
//            if (classInfo.serializeAs != null) {
//                classInfo = Classes.getExactClassInfo(classInfo.serializeAs as Class<Any>)
//                    ?: throw IllegalArgumentException("No class info found for ${classInfo.serializeAs}")
//            }
//
//            val serializableObj =
//                Converters.convert(serializable, classInfo.c) ?: throw IllegalArgumentException("Cannot convert ${serializable::class.java} to ${classInfo.c}")
//
//            require(classInfo.serializer != null) { "No serializer found for ${classInfo.c}" }
//
//            val outputStream = ByteArrayOutputStream()
//            Variables.yggdrasil.newOutputStream(outputStream).use {
//                it.writeObject(serializableObj)
//                it.flush()
//            }
//            return outputStream
//        }
//
//        fun deserialize(data: InputStream, typeCode: String): Any {
//            val classInfo = Classes.getClassInfo(typeCode)
//            return Classes.deserialize(classInfo, data)
//                ?: throw IllegalArgumentException(
//                    "Failed to deserialize Skript value of type '$typeCode' (${classInfo.c.name})."
//                )
//        }
//    }

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
