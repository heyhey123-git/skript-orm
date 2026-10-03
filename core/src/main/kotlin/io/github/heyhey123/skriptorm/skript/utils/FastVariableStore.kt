package io.github.heyhey123.skriptorm.skript.utils

import ch.njol.skript.lang.Variable
import ch.njol.skript.variables.Variables
import io.github.heyhey123.skriptorm.SkriptOrm
import org.bukkit.event.Event
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.Comparator
import java.util.Locale
import java.util.TreeMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReadWriteLock

/**
 * Prepares a large result's list-variable tree on the database thread, then attaches it to Skript's
 * global variable store on the server thread. This avoids building the tree one index at a time
 * while holding Skript's variable lock.
 *
 * The path applies only to results with at least [MIN_VALUES] entries and only after a probe has
 * checked the expected Skript internals. It uses linked [MethodHandle]s for those internal fields
 * and methods. [VariableModifier] uses Skript's regular API when the fast path is unavailable.
 */
internal object FastVariableStore {

    /**
     * Minimum result size for building and attaching a tree through this path.
     */
    const val MIN_VALUES = 10_000

    private const val VARIABLES_CLASS = "ch.njol.skript.variables.Variables"
    private const val VARIABLES_MAP_CLASS = "ch.njol.skript.variables.VariablesMap"
    private const val NODE_CLASS = "org.skriptlang.skript.util.IndexTrackingTreeMap"
    private const val SCRATCH = "skriptorm_faststore_scratch"

    /** True when every internal this fast path needs was found and validated. */
    @Volatile
    var available = false
        private set

    /** Why the fast path is off, for the one line logged when it is first probed. */
    var diagnosis = "not probed"
        private set

    private var probed = false
    private lateinit var rootTree: MutableMap<Any?, Any?>
    private lateinit var rootHash: MutableMap<Any?, Any?>
    private lateinit var processChangeQueue: MethodHandle
    private lateinit var saveVariableChange: MethodHandle
    private lateinit var nodeConstructor: MethodHandle
    private lateinit var nodeComparator: Comparator<String>
    private lateinit var lock: ReadWriteLock

    private val held = ConcurrentHashMap<Int, Pair<Any, Prepared>>()

    /** A result tree built off the server thread, ready to be attached in one step. */
    class Prepared(
        val subtree: MutableMap<Any?, Any?>,
        val names: List<String>,
        val values: List<Any?>
    )

    /** Resolves and validates everything the fast path needs. Must run on the server thread. */
    fun probe() {
        if (available || probed) return
        probed = true
        try {
            val variables = Class.forName(VARIABLES_CLASS)
            val variablesMap = Class.forName(VARIABLES_MAP_CLASS)
            val lookup = MethodHandles.privateLookupIn(variables, MethodHandles.lookup())

            val variablesField = lookup.findStaticGetter(variables, "variables", variablesMap)
            val lockField = lookup.findStaticGetter(
                variables,
                "variablesLock",
                variables.getDeclaredField("variablesLock").type
            )
            processChangeQueue = lookup.findStatic(
                variables,
                "processChangeQueue",
                MethodType.methodType(Void.TYPE)
            )
            saveVariableChange = lookup.findStatic(
                variables,
                "saveVariableChange",
                MethodType.methodType(Void.TYPE, String::class.java, Any::class.java)
            )

            val mapLookup = MethodHandles.privateLookupIn(variablesMap, MethodHandles.lookup())
            val treeGetter = mapLookup.findGetter(
                variablesMap,
                "treeMap",
                variablesMap.getDeclaredField("treeMap").type
            )
            val hashGetter = mapLookup.findGetter(
                variablesMap,
                "hashMap",
                variablesMap.getDeclaredField("hashMap").type
            )

            lock = lockField.invoke() as ReadWriteLock
            val map = variablesField.invoke()
            @Suppress("UNCHECKED_CAST")
            rootTree = treeGetter.invoke(map) as MutableMap<Any?, Any?>
            @Suppress("UNCHECKED_CAST")
            rootHash = hashGetter.invoke(map) as MutableMap<Any?, Any?>

            // Build a scratch list with Skript, then reuse its node class and comparator.
            Variables.setVariable("$SCRATCH::node::leaf", 1, null, false)
            val sample = rootTree[SCRATCH]
            val nodeClass = Class.forName(NODE_CLASS)
            require(sample != null && nodeClass.isInstance(sample)) {
                "expected a ${NODE_CLASS.substringAfterLast('.')} node, found ${sample?.javaClass?.name ?: "null"}"
            }
            @Suppress("UNCHECKED_CAST")
            nodeComparator = (sample as TreeMap<String, Any?>).comparator() as Comparator<String>
            nodeConstructor = MethodHandles.privateLookupIn(nodeClass, MethodHandles.lookup())
                .findConstructor(nodeClass, MethodType.methodType(Void.TYPE, Comparator::class.java))
            val built = newNode()
            require(nodeClass.isInstance(built)) { "constructor produced ${built.javaClass.name}" }
            Variables.setVariable("$SCRATCH::node::leaf", null, null, false)

            available = true
            diagnosis = "ready"
        } catch (error: Throwable) {
            available = false
            diagnosis = "${error.javaClass.simpleName}: ${error.message}"
        }
        SkriptOrm.instance.logger.info("[faststore] available=$available ($diagnosis)")
    }

    @Suppress("UNCHECKED_CAST")
    private fun newNode(): MutableMap<Any?, Any?> = nodeConstructor.invoke(nodeComparator) as MutableMap<Any?, Any?>

    /**
     * Builds a result tree for [take] when the fast path is available and the result is large enough.
     */
    @Suppress("UNCHECKED_CAST")
    fun publish(rows: Map<String, Any?>) {
        if (!available || rows.size < MIN_VALUES) return
        val lower = Variables.caseInsensitiveVariables
        val names = ArrayList<String>(rows.size)
        val values = ArrayList<Any?>(rows.size)
        val subtree = newNode()
        for ((key, raw) in rows) {
            // Let the regular Skript path handle null values, which represent absent keys.
            if (raw == null) return
            val name = if (lower) key.lowercase(Locale.ENGLISH) else key
            val segments = name.split("::")
            var node = subtree
            for (index in 0 until segments.size - 1) {
                var child = node[segments[index]]
                if (child !is MutableMap<*, *>) {
                    child = newNode()
                    node[segments[index]] = child
                }
                node = child as MutableMap<Any?, Any?>
            }
            node[segments.last()] = raw
            names.add(name)
            values.add(raw)
        }
        if (held.size > 8) held.clear()
        held[System.identityHashCode(rows)] = rows to Prepared(subtree, names, values)
    }

    /** Takes the tree prepared for [rows], if one is still waiting. */
    fun take(rows: Map<String, Any?>): Prepared? {
        val entry = held.remove(System.identityHashCode(rows)) ?: return null
        if (entry.first !== rows) return null
        return entry.second
    }

    /**
     * Attaches [prepared] to the global [variable]. Returns false when the variable or store shape
     * cannot use this path, allowing the caller to use Skript's regular API.
     */
    @Suppress("UNCHECKED_CAST")
    fun attach(variable: Variable<*>, event: Event?, prepared: Prepared): Boolean {
        if (!available || variable.isLocal) return false
        val raw = variable.toString(event, false)
        if (raw.length < 4 || raw[0] != '{' || raw[raw.length - 1] != '}') return false
        val list = raw.substring(1, raw.length - 1)
        if (!list.endsWith("*")) return false
        val path = list.dropLast(1).removeSuffix("::")
        if (path.isEmpty()) return false
        val segments = path.split("::")
        if (segments.any { it.isEmpty() }) return false
        val base = if (Variables.caseInsensitiveVariables) path.lowercase(Locale.ENGLISH) else path

        val writeLock = lock.writeLock()
        writeLock.lock()
        try {
            processChangeQueue.invoke()
            var parent: MutableMap<Any?, Any?> = rootTree
            for (index in 0 until segments.size - 1) {
                val child = parent[segments[index]]
                parent = when {
                    child == null -> newNode().also { parent[segments[index]] = it }
                    child is MutableMap<*, *> -> child as MutableMap<Any?, Any?>
                    else -> return false
                }
            }
            val leaf = segments.last()
            val existing = parent[leaf]
            if (existing is MutableMap<*, *>) {
                // Preserve any scalar value stored directly under the list name.
                removeNames(base, existing)
            } else if (existing != null) {
                return false
            }

            parent[leaf] = prepared.subtree
            for (index in prepared.names.indices) {
                val name = "$base::${prepared.names[index]}"
                val value = prepared.values[index]
                rootHash[name] = value
                saveVariableChange.invoke(name, value)
            }
            return true
        } finally {
            writeLock.unlock()
        }
    }

    /** Drops every full name under [prefix] from Skript's root hash map. */
    private fun removeNames(prefix: String, node: Map<*, *>) {
        for ((key, value) in node) {
            // Preserve the scalar value at the list root.
            if (key == null) continue
            val name = "$prefix::$key"
            rootHash.remove(name)
            if (value is Map<*, *>) removeNames(name, value)
        }
    }
}
