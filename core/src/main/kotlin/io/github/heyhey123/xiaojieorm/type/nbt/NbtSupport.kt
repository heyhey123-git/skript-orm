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
 * gives scripts a way to write a compound in the first place (`nbt compound from "{...}"`), so a value
 * in a script is always one of SkBee's compounds. SkBee bundles the NBT library it uses under its own
 * package, `com.shanebeestudios.skbee.api.nbt`, so nothing here can be compiled against and every
 * reference is looked up by name instead.
 *
 * Looking them up needs care, because SkBee is loaded as a *Paper* plugin: Paper does not put a Paper
 * plugin's classes where a Bukkit plugin's own class loader can see them, so `Class.forName` from here
 * fails even while SkBee is running. The plugin therefore hands this object a lookup for SkBee's own
 * loader while it enables (see [useClassLoaderLookup]), and that loader is tried after this plugin's.
 * Keeping the lookup a parameter is also what keeps a Bukkit reference out of this file, because the
 * unit tests run without a server and still have to be able to load it.
 *
 * The calls themselves go through [MethodHandle] rather than `Method.invoke`: they are linked once
 * when the classes are resolved, and every converted value then passes through a call the JIT can
 * inline, with no argument array and no access check per value.
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

    /** Set while the plugin enables; see [useClassLoaderLookup]. */
    @Volatile
    private var providedLookup: (() -> ClassLoader?)? = null

    @Volatile
    private var resolved: Resolution? = null

    /**
     * Registers where SkBee's classes can be found.
     *
     * The plugin calls this with a lookup that asks Bukkit for the SkBee plugin and takes its class
     * loader. Installing it clears the cached answer, so a lookup that arrives after something already
     * asked about NBT still takes effect.
     */
    fun useClassLoaderLookup(lookup: () -> ClassLoader?) {
        providedLookup = lookup
        resolved = null
    }

    private val resolution: Resolution
        get() = resolved ?: resolveOnce()

    private fun resolveOnce(): Resolution {
        val provided = providedLookup?.invoke()
        val loaders = buildList {
            NbtSupport::class.java.classLoader?.let { add(it) }
            if (provided != null && provided !in this) add(provided)
        }
        // A lookup that answers means the plugin is there, which is worth saying: "not installed" and
        // "installed but unreachable" are different problems with different fixes.
        return Flavour.resolve(SKBEE_ROOT, loaders, provided != null).also { resolved = it }
    }

    /** Whether NBT values can be handled at all on this server. */
    val isAvailable: Boolean
        get() = resolution.flavour != null

    /** The name of the plugin the NBT classes come from, for logs and messages. */
    val provider: String
        get() = if (isAvailable) "SkBee" else "none"

    /**
     * Why NBT cannot be used here, or null when it can. Registering a table that declares an NBT
     * column is refused with this sentence, so it says which of the two problems it is.
     */
    val unavailableReason: String?
        get() = resolution.problem

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
        invoke("Failed to serialize NBT") { flavour.writeApiNbt.invoke(null, value, output) }
        return output.toByteArray()
    }

    /**
     * A detached compound read from NBT [bytes].
     *
     * @throws IllegalArgumentException if the bytes are not a compound
     */
    fun fromBytes(bytes: ByteArray): Any {
        val flavour = requireFlavour("read a stored compound")
        return invoke("Failed to deserialize NBT") {
            flavour.containerFromStream.invoke(ByteArrayInputStream(bytes))
        } ?: throw IllegalArgumentException("The stored data holds no NBT compound.")
    }

    /**
     * Runs a linked call, reporting a failure as [description] went wrong.
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
     * The three handles are the library's own ways in and out: `NBTContainer(String)` parses SNBT,
     * `NBTContainer(InputStream)` reads NBT bytes, and `NBTReflectionUtil.writeApiNBT` writes a
     * compound to a stream. Reading bytes this way costs one call instead of two, because the
     * container wraps the NMS tag that `readNBT` returns.
     *
     * @property compoundClass the compound interface, which is what a column holds and what a script's
     *           value is an instance of
     * @property containerFromString builds a detached compound from SNBT
     * @property containerFromStream builds a detached compound from NBT bytes
     * @property writeApiNbt writes a compound to a stream as NBT
     */
    private class Flavour(
        val compoundClass: Class<*>,
        val containerFromString: MethodHandle,
        val containerFromStream: MethodHandle,
        val writeApiNbt: MethodHandle
    ) {

        companion object {

            /** This plugin's own lookup, which is what makes the handles below callable from here. */
            private val lookup: MethodHandles.Lookup = MethodHandles.lookup()

            /**
             * Links the classes in [root], trying each of [loaders] in turn.
             *
             * [pluginFound] says whether the plugin itself was seen, which decides how a failure reads:
             * a server without SkBee is a normal state, while an SkBee that cannot be linked is an SkBee
             * this version does not understand, and saying so saves the reader a hunt.
             */
            fun resolve(root: String, loaders: List<ClassLoader>, pluginFound: Boolean): Resolution {
                var linkage: String? = null
                for (loader in loaders) {
                    try {
                        val compound = Class.forName("$root.NBTCompound", false, loader)
                        val container = Class.forName("$root.NBTContainer", false, loader)
                        val reflection = Class.forName("$root.NBTReflectionUtil", false, loader)
                        return Resolution(
                            Flavour(
                                compound,
                                lookup.findConstructor(
                                    container,
                                    MethodType.methodType(Void.TYPE, String::class.java)
                                ),
                                lookup.findConstructor(
                                    container,
                                    MethodType.methodType(Void.TYPE, InputStream::class.java)
                                ),
                                lookup.findStatic(
                                    reflection,
                                    "writeApiNBT",
                                    MethodType.methodType(Void.TYPE, compound, OutputStream::class.java)
                                )
                            ),
                            null
                        )
                    } catch (_: ClassNotFoundException) {
                        continue
                    } catch (error: ReflectiveOperationException) {
                        linkage = error.message
                    }
                }
                val problem = when {
                    linkage != null ->
                        "SkBee is installed, but its NBT classes under $root could not be linked ($linkage)"
                    pluginFound ->
                        "SkBee is installed, but its NBT classes under $root were not found"
                    else ->
                        "SkBee is not installed, and it is what provides NBT compounds"
                }
                return Resolution(null, problem)
            }
        }
    }
}
