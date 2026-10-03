package io.github.heyhey123.skriptorm.type.nbt

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

/**
 * Accesses SkBee's NBT classes without a compile-time dependency. The classes are resolved through
 * SkBee's class loader, which Paper may keep separate from this addon's loader. Calls use linked
 * [MethodHandle]s.
 *
 * SkBee compounds may wrap live Bukkit objects. [normalize] converts them to detached compounds
 * on the server thread before asynchronous database work begins. SNBT preserves the compound's
 * tags during that conversion.
 */
object NbtSupport {

    /**
     * Package containing SkBee's relocated NBT classes.
     */
    private const val SKBEE_ROOT = "com.shanebeestudios.skbee.api.nbt"

    /**
     * Package used for class lookup; tests substitute stand-in classes here.
     */
    @Volatile
    private var providerRoot: String = SKBEE_ROOT

    /** Set while the plugin enables; see [useClassLoaderLookup]. */
    @Volatile
    private var providedLookup: (() -> ClassLoader?)? = null

    @Volatile
    private var resolved: Resolution? = null

    /**
     * Registers where SkBee's classes can be found.
     *
     * The plugin supplies SkBee's class loader through [lookup]. Replacing the lookup clears any
     * cached resolution.
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
        // Distinguish a missing SkBee plugin from one whose classes cannot be resolved.
        return Flavour.resolve(providerRoot, loaders, provided != null).also { resolved = it }
    }

    /**
     * Points the lookup at [root] instead of SkBee's package.
     *
     * Tests use stand-in classes under [root] to exercise method-handle linking without a server.
     */
    internal fun useProviderForTesting(root: String) {
        providerRoot = root
        resolved = null
    }

    /** Puts the lookup back to SkBee's package and forgets what was resolved. */
    internal fun resetProviderForTesting() {
        providerRoot = SKBEE_ROOT
        resolved = null
    }

    /** Whether NBT values can be handled at all on this server. */
    val isAvailable: Boolean
        get() = resolution.flavour != null

    /** The name of the plugin the NBT classes come from, for logs and messages. */
    val provider: String
        get() = if (isAvailable) "SkBee" else "none"

    /**
     * Reason NBT is unavailable, or null when it can be used. Included in the error for an NBT
     * column registered without a working SkBee integration.
     */
    val unavailableReason: String?
        get() = resolution.problem

    /**
     * SkBee's compound interface, or `Any` as a placeholder when SkBee is unavailable. Tables with
     * NBT columns cannot be registered in the latter case.
     */
    val domainType: Class<*>
        get() = resolution.flavour?.compoundClass ?: Any::class.java

    /** Whether [value] is one of SkBee's compounds. */
    fun isNbt(value: Any?): Boolean =
        value != null && resolution.flavour?.compoundClass?.isInstance(value) == true

    /**
     * Returns [value] as SNBT. Call on the server thread because it may wrap a live object.
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
     * Detaches a SkBee compound from any live object it wraps. Other values pass through unchanged.
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
        invoke("Failed to serialize NBT") { flavour.writeApiNbt.invoke(value, output) }
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
     * Runs a linked call and includes its underlying failure in the error message.
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
     * Linked access to SkBee's compound interface, SNBT and byte-stream constructors, and NBT
     * writer. The stream constructor reads bytes directly into a detached container.
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

            /** Lookup used to link the method handles below. */
            private val lookup: MethodHandles.Lookup = MethodHandles.lookup()

            /**
             * Links the classes in [root], trying each of [loaders] in turn.
             *
             * [pluginFound] distinguishes a missing plugin from an incompatible SkBee version in
             * the failure message.
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
