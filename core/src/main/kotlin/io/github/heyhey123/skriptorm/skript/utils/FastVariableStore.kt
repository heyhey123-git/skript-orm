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
 * Stores a large read result into a global Skript list variable without making the server thread walk
 * Skript's variable tree one value at a time.
 *
 * Skript builds the list tree value by value while holding its variable lock: every value costs a
 * lowercased full name, a split, a descent and two map writes, all on the server thread. The tree depends
 * on nothing but the values, so it can be built on the database thread and attached in one step.
 *
 * Only results of at least [MIN_VALUES] values take this path, and only when the probe below found a Skript
 * whose variable store looks exactly as expected; the probe runs when the first such result arrives, so that
 * one result is still stored the ordinary way. Measured on Paper with Skript 2.16.2, storing 30000
 * values costs about 47ms on the server thread the ordinary way and about 36ms this way; at 600 values the
 * two are even. What remains on the server thread either way is the part that cannot move: the per-value
 * save to Skript's variable storage, and the root hash map that answers reads of single indexes.
 *
 * Everything Skript does not expose publicly is reached through [MethodHandle]s, resolved once and then
 * validated by doing the work for real on a throwaway variable. If anything about the shape differs, the
 * path stays off and [VariableModifier] stores the result the ordinary way, which is always correct.
 */
internal object FastVariableStore {

    /**
     * Below this many values the graft costs about as much as it saves, so small results never touch any of
     * this. At 600 values the two paths measured even, and the gain grows with the size of the result.
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

            // Ask Skript itself what a list node looks like: write a throwaway variable, take the node it
            // built, and reuse that node's own class and comparator for everything built afterwards.
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
     * Builds the result tree on the database thread, ready for [take]. Does nothing when the fast path is
     * off or the result is too small, and nothing but a wasted build when the caller then finds it unusable.
     */
    @Suppress("UNCHECKED_CAST")
    fun publish(rows: Map<String, Any?>) {
        if (!available || rows.size < MIN_VALUES) return
        val lower = Variables.caseInsensitiveVariables
        val names = ArrayList<String>(rows.size)
        val values = ArrayList<Any?>(rows.size)
        val subtree = newNode()
        for ((key, raw) in rows) {
            // In Skript's eyes a missing column is a delete; leave that decision to Skript.
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
     * Attaches [prepared] to [variable], replacing whatever it held. Answers false when the fast path
     * cannot finish, in which case the caller stores the result the ordinary way.
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
                // Skript keeps a value stored directly under a list: deleting the list restores it, so such
                // a value's own entry in the root map is left exactly where it is.
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
            // A value stored directly under the list belongs to the list's own name, which stays.
            if (key == null) continue
            val name = "$prefix::$key"
            rootHash.remove(name)
            if (value is Map<*, *>) removeNames(name, value)
        }
    }
}
