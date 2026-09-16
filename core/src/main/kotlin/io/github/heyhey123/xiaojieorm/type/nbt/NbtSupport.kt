package io.github.heyhey123.xiaojieorm.type.nbt

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

/**
 * Access to SkBee's NBT classes, without compiling against them.
 *
 * This plugin ships no NBT implementation. SkBee is the only one it supports, because SkBee is what
 * gives scripts a way to write a compound in the first place (`nbt compound from "{...}"`); a value in
 * a script is therefore always one of SkBee's compounds. SkBee bundles the NBT library it uses under
 * its own package, `com.shanebeestudios.skbee.api.nbt`, so the standalone NBT API plugin and SkBee
 * cannot see each other's classes, and neither can this plugin at compile time. Every reference here
 * is looked up by name, once, and SkBee absent is a normal state rather than an error: a server
 * without it can still connect, and only a table that declares an NBT column is refused.
 *
 * The calls themselves go through [MethodHandle] rather than `Method.invoke`: they are linked once
 * when the classes are resolved, and every converted value then passes through a call that the JIT
 * can inline, with no argument array and no access check per value.
 *
 * SNBT, the text form of a compound, is the interchange with SkBee's live values. It is faithful:
 * every tag type, list and array survives it.
 *
 * A compound SkBee hands over is not plain data. `NBTCustomItemStack`, for instance, is an NBT-API
 * container that also holds the item it came from, and NBT-API writes a change to that compound back
 * to the item through Bukkit when it saves one. Two things follow, and [normalize] is what deals with
 * both: carrying such a value into the asynchronous query layer would read the object long after the
 * script asked for it, and a change made there would reach for a live object from the wrong thread.
 * Normalising replaces it, on the server thread, with a detached compound holding the same data.
 */
object NbtSupport {

    /**
     * The package SkBee relocates the NBT library into. SkBee's own NBT classes sit beside it, and the
     * names used below are the library's, so this is the only thing that would change with SkBee.
     */
    private const val SKBEE_ROOT = "com.shanebeestudios.skbee.api.nbt"

    private val resolution: Resolution = Flavour.resolve(SKBEE_ROOT)

    /** Whether NBT values can be handled at all on this server. */
    val isAvailable: Boolean
        get() = resolution.flavour != null

    /**
     * The class an NBT column holds: SkBee's compound interface, or `Any` when SkBee is absent. A
     * permissive domain is only a placeholder in that case, because no script can produce an NBT value
     * without SkBee and a table declaring an NBT column is refused when it is registered.
     */
    val domainType: Class<*>
        get() = resolution.flavour?.compoundClass ?: Any::class.java

    /** Whether [value] is one of SkBee's compounds. */
    fun isNbt(value: Any?): Boolean =
        value != null && resolution.flavour?.compoundClass?.isInstance(value) == true

    /**
     * The SNBT text of [value]. Called while the script's event is still at hand, because [value] may
     * be a view over a live object; see the class documentation.
     *
     * @throws IllegalArgumentException if [value] is not one of SkBee's compounds
     */
    fun snbt(value: Any): String {
        val flavour = requireFlavour("read a compound from a script")
        require(flavour.compoundClass.isInstance(value)) {
            "Expected ${flavour.compoundClass.name}, but found ${value.javaClass.name}."
        }
        return value.toString()
    }

    /**
     * A detached compound parsed from [snbt], safe to use from any thread.
     *
     * @throws IllegalArgumentException if [snbt] is not a valid compound
     */
    fun parseSnbt(snbt: String): Any {
        val flavour = requireFlavour("build a compound")
        return invoke("Failed to parse NBT: $snbt") { flavour.containerFromString.invoke(snbt) }
            ?: throw IllegalArgumentException("Failed to parse NBT: $snbt")
    }

    /**
     * A detached compound for [value], or [value] itself when it is not one of SkBee's compounds.
     *
     * This is the conversion that makes a script's value safe to carry: it is taken from the live
     * object at the moment the script names it, and what travels into the query layer is a plain
     * compound with the same tags and nothing behind it.
     */
    fun normalize(value: Any?): Any? {
        if (!isNbt(value)) return value
        return parseSnbt(snbt(value as Any))
    }

    /**
     * The NBT bytes of [value], which must be one of SkBee's compounds.
     *
     * @throws IllegalArgumentException if [value] is not one of SkBee's compounds
     */
    fun toBytes(value: Any): ByteArray {
        val flavour = requireFlavour("store a compound")
        require(flavour.compoundClass.isInstance(value)) {
            "Expected ${flavour.compoundClass.name}, but found ${value.javaClass.name}."
        }
        val output = ByteArrayOutputStream()
        invoke("Failed to serialize NBT") { flavour.writeCompound.invoke(value, output) }
        return output.toByteArray()
    }

    /**
     * A detached compound read from NBT [bytes].
     *
     * @throws IllegalArgumentException if the bytes are not a compound
     */
    fun fromBytes(bytes: ByteArray): Any {
        val flavour = requireFlavour("read a stored compound")
        val compound = ByteArrayInputStream(bytes).use { input ->
            invoke("Failed to deserialize NBT") { flavour.readNbt.invoke(input) }
        }
        return compound ?: throw IllegalArgumentException("The stored data holds no NBT compound.")
    }

    /**
     * Runs a linked call, reporting a failure as [what][description] went wrong.
     *
     * A handle throws whatever the target throws, wrapped in a throwable of its own, so the cause is
     * what carries the useful message.
     */
    private inline fun invoke(description: String, call: () -> Any?): Any? = try {
        call()
    } catch (error: Throwable) {
        throw IllegalArgumentException("$description: ${error.cause?.message ?: error.message}", error.cause ?: error)
    }

    private fun requireFlavour(action: String): Flavour = resolution.flavour
        ?: throw IllegalStateException("Cannot $action because ${resolution.problem}.")

    /** What was found while resolving SkBee: the classes, or why they could not be used. */
    private class Resolution(val flavour: Flavour?, val problem: String?)

    /**
     * SkBee's NBT classes, linked once by package.
     *
     * @property compoundClass the compound interface, which is what a column holds and what a script's
     *           value is an instance of
     * @property containerFromString builds a detached compound from SNBT
     * @property writeCompound writes a compound to a stream as NBT
     * @property readNbt reads a compound from a stream of NBT
     */
    private class Flavour(
        val compoundClass: Class<*>,
        val containerFromString: MethodHandle,
        val writeCompound: MethodHandle,
        val readNbt: MethodHandle
    ) {

        companion object {

            /** The plugin's own lookup, which is what makes the handles below callable from here. */
            private val lookup: MethodHandles.Lookup = MethodHandles.lookup()

            /**
             * Links the classes in [root], reporting why when they cannot be used.
             *
             * SkBee missing is the normal case and is reported as such, because a server without it
             * is expected to work; a class that is present but shaped differently means an SkBee
             * version this plugin does not understand, which is worth saying out loud. The loader is
             * the plugin's own, and it reaches SkBee's classes the same way this plugin reaches
             * Skript's, so nothing here depends on load order beyond `softdepend`.
             */
            fun resolve(root: String): Resolution = try {
                val loader = NbtSupport::class.java.classLoader
                val compound = Class.forName("$root.NBTCompound", false, loader)
                val container = Class.forName("$root.NBTContainer", false, loader)
                val reflection = Class.forName("$root.NBTReflectionUtil", false, loader)
                Resolution(
                    Flavour(
                        compound,
                        lookup.findConstructor(
                            container,
                            MethodType.methodType(Void.TYPE, String::class.java)
                        ),
                        lookup.findVirtual(
                            compound,
                            "writeCompound",
                            MethodType.methodType(Void.TYPE, OutputStream::class.java)
                        ),
                        lookup.findStatic(
                            reflection,
                            "readNBT",
                            MethodType.methodType(compound, InputStream::class.java)
                        )
                    ),
                    null
                )
            } catch (_: ClassNotFoundException) {
                Resolution(null, "SkBee is not installed, and it is what provides NBT compounds")
            } catch (error: ReflectiveOperationException) {
                Resolution(
                    null,
                    "SkBee's NBT classes under $root are not the ones this plugin expects " +
                        "(${error.message})"
                )
            }
        }
    }
}
