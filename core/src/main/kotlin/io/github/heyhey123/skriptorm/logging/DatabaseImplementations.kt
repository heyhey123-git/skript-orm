package io.github.heyhey123.skriptorm.logging

import io.github.heyhey123.skriptorm.database.DatabaseRegistry

/**
 * What the plugin found when it looked for the database implementations it was built with.
 *
 * The implementations are discovered by name, because nothing in this module may depend on them: the core
 * is compiled without them, the jar carries whichever ones the build bundled, and the list of names is the
 * one thing that has to know. That list can be wrong in a way nothing reports — a name that does not match
 * a class, or a class whose own initialization fails, leaves a type a script cannot connect to and a console
 * that says nothing. This is what turns that silence into lines.
 *
 * What it is not is a decision: an implementation that failed to load is one a server may never have
 * needed, so startup carries on either way. What changes is that a server owner who expected a type now has
 * the list they did get, and the names they did not.
 *
 * The work is separate from the logging so that a full load and a broken one are both unit-testable: the
 * second is otherwise only reachable by building a jar without a module, or with a broken one.
 */
internal object DatabaseImplementations {

    /**
     * Forces each candidate class to load, then reports which database types came of it.
     *
     * @param candidates the class names this build bundled, in the order the build lists them
     * @param loader the class loader the candidates are read from — the plugin's own, so that the
     *   implementations bundled in its jar are found and nothing else on the server is
     * @return what loaded, and what did not
     */
    fun discover(candidates: List<String>, loader: ClassLoader): ImplementationReport {
        val failed = mutableListOf<String>()
        for (candidate in candidates) {
            try {
                Class.forName(candidate, true, loader)
            } catch (error: Throwable) {
                // A candidate that is not in this jar is a build without that module, and one whose
                // initialization fails is a broken implementation: either way the server keeps running
                // with the rest, and the report is where the difference is written down.
                failed += "$candidate (${reasonFor(error)})"
            }
        }
        return ImplementationReport(DatabaseRegistry.registered().map { it.typeName }.sorted(), failed)
    }

    /**
     * The lines a server's console gets for what [discover] found.
     *
     * The summary is printed even when everything loaded, because the list is the interesting part: those
     * are the names a script may write after `database`, so a server owner who expected one of them and does
     * not see it has the answer without reading the changelog. Only the candidates that failed get a line of
     * their own, since the summary already names what worked.
     */
    fun lines(report: ImplementationReport): List<String> {
        val summary = if (report.loaded.isEmpty()) {
            "Database implementations: none loaded. No script can connect to a database."
        } else {
            "Database implementations: ${report.loaded.joinToString(", ")}."
        }
        return listOf(summary) + report.failed.map {
            "Database implementation $it did not load, so its type is not available."
        }
    }

    /**
     * Why a candidate could not be loaded, in the words of whatever actually went wrong.
     *
     * A failed class initialization is reported by the JVM as an error whose own message is null and whose
     * cause holds the exception the initializer threw. Reading only the outer message would print the name of
     * a JVM error class and nothing else, which is the kind of line that looks like information and is not;
     * the deepest message in the chain is therefore the one that is used.
     */
    private fun reasonFor(error: Throwable): String {
        var deepest: String? = null
        var current: Throwable? = error
        val seen = mutableSetOf<Throwable>()
        while (current != null && seen.add(current)) {
            current.message?.takeIf { it.isNotBlank() }?.let { deepest = it }
            current = current.cause
        }
        return deepest ?: error.javaClass.simpleName
    }
}

/**
 * What a discovery run found.
 *
 * @property loaded the database type names a script can write, one per implementation that registered
 * @property failed one entry per candidate that could not be loaded, naming the candidate and why
 */
internal data class ImplementationReport(
    val loaded: List<String>,
    val failed: List<String>
) {

    /** Whether every candidate was loaded. */
    val complete: Boolean
        get() = failed.isEmpty()
}
