package io.github.heyhey123.xiaojieorm.type.nbt.standin

import java.io.InputStream
import java.io.OutputStream

/**
 * Stand-ins for the three members of SkBee's NBT library that [io.github.heyhey123.xiaojieorm.type.nbt.NbtSupport]
 * links, with the same shapes and nothing else.
 *
 * They exist so the unit tests can drive the whole chain, from linking the handles to writing and
 * reading bytes, without a server and without SkBee. A handle linked with the wrong parameter types is
 * a mistake that only shows up when it is called, and a server used to be the only place that happened;
 * these classes move that discovery into `./gradlew test`.
 *
 * The encoding is not NBT: it is SNBT as UTF-8, which is enough to prove that the data goes out and
 * comes back through the handles. The real bytes are the library's business.
 */
interface NBTCompound

class NBTContainer : NBTCompound {

    private val snbt: String

    constructor(snbt: String) {
        this.snbt = snbt
    }

    constructor(input: InputStream) {
        this.snbt = input.readBytes().toString(Charsets.UTF_8)
    }

    override fun toString(): String = snbt
}

object NBTReflectionUtil {

    @JvmStatic
    fun writeApiNBT(compound: NBTCompound, stream: OutputStream) {
        stream.write(compound.toString().toByteArray(Charsets.UTF_8))
    }
}
