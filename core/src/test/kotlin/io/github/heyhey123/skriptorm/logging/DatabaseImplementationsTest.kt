package io.github.heyhey123.skriptorm.logging

import io.github.heyhey123.skriptorm.database.Database
import io.github.heyhey123.skriptorm.database.DatabaseFactory
import io.github.heyhey123.skriptorm.database.DatabaseRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Covers what the plugin says at startup about the implementations it was built with.
 *
 * The report exists because a candidate is looked up by name: a name that does not match a class, or a class
 * whose initialization fails, leaves a database type a script cannot connect to and used to leave the console
 * silent about it. Neither is reachable by building the jar normally, so both are reached here — the missing
 * one through a class loader that refuses everything but this file's own stubs, and the broken one through a
 * stub whose initializer throws.
 *
 * The registry lives for the whole test JVM, so the cases below assert what *they* registered rather than the
 * whole set: a suite that named every registration would be asserting an order of other tests.
 */
class DatabaseImplementationsTest {

    @Test
    fun `a loaded candidate is reported as a type a script can write`() {
        DatabaseRegistry.register(ProbeOneFactory)

        val report = DatabaseImplementations.discover(
            listOf(ProbeOneFactory::class.java.name),
            onlyThisFile()
        )

        assertTrue("ProbeOne" in report.loaded, report.loaded.toString())
        assertTrue(report.complete, "nothing failed, so the load is complete")
        assertEquals(
            "Database implementations: ${report.loaded.joinToString(", ")}.",
            DatabaseImplementations.lines(report).single()
        )
    }

    @Test
    fun `a candidate that is not there is named, and the rest still load`() {
        DatabaseRegistry.register(ProbeTwoFactory)

        val report = DatabaseImplementations.discover(
            listOf(ProbeTwoFactory::class.java.name, "com.example.NoSuchFactory"),
            onlyThisFile()
        )

        assertTrue("ProbeTwo" in report.loaded, report.loaded.toString())
        assertEquals(false, report.complete)
        assertEquals(1, report.failed.size)
        assertTrue(report.failed.single().startsWith("com.example.NoSuchFactory ("), report.failed.single())

        val lines = DatabaseImplementations.lines(report)
        assertEquals(
            "Database implementations: ${report.loaded.joinToString(", ")}.",
            lines.first()
        )
        assertTrue(
            "Database implementation com.example.NoSuchFactory" in lines[1] &&
                "its type is not available" in lines[1],
            lines[1]
        )
    }

    /**
     * A class whose *initialization* fails is what a name check would miss: the class is there and
     * `Class.forName` still throws, and the JVM reports it as an error whose own message is null. The report
     * has to carry what the initializer actually threw, because "did not load" without a reason is the same
     * silence this was written to remove.
     */
    @Test
    fun `a candidate that throws while loading is reported with the cause it threw`() {
        val report = DatabaseImplementations.discover(
            listOf(BrokenProbeFactory::class.java.name),
            onlyThisFile()
        )

        assertEquals(1, report.failed.size)
        assertEquals(
            "${BrokenProbeFactory::class.java.name} (deliberately broken)",
            report.failed.single()
        )
        val line = DatabaseImplementations.lines(report).first { "BrokenProbeFactory" in it }
        assertEquals(
            "Database implementation ${BrokenProbeFactory::class.java.name} (deliberately broken) did not " +
                "load, so its type is not available.",
            line
        )
    }

    /**
     * A build with no implementation at all, which is a shape the discovery function cannot reach here — the
     * registry always holds whatever this JVM registered — so it is asserted where it is decided.
     */
    @Test
    fun `a load that found nothing says so instead of printing an empty list`() {
        val report = ImplementationReport(loaded = emptyList(), failed = emptyList())

        assertEquals(
            listOf("Database implementations: none loaded. No script can connect to a database."),
            DatabaseImplementations.lines(report)
        )
    }

    /**
     * A loader that serves this file's own stubs and refuses everything else, so a candidate can be made to
     * fail deterministically without building a jar without its module.
     */
    private fun onlyThisFile(): ClassLoader =
        object : ClassLoader(DatabaseImplementationsTest::class.java.classLoader) {
            override fun loadClass(name: String, resolve: Boolean): Class<*> {
                if (name.startsWith(ProbeOneFactory::class.java.name.substringBeforeLast('.'))) {
                    return super.loadClass(name, resolve)
                }
                throw ClassNotFoundException("$name is not in this build")
            }
        }
}

/** A stub implementation that registers itself under a name of its own. */
private object ProbeOneFactory : StubFactory("ProbeOne")

/** A second stub, for the case that pairs a loaded candidate with one that is not there. */
private object ProbeTwoFactory : StubFactory("ProbeTwo")

/** An implementation whose initialization fails, which is what a broken candidate looks like. */
private object BrokenProbeFactory : DatabaseFactory {

    init {
        throw IllegalStateException("deliberately broken")
    }

    override val typeName: String = "BrokenProbe"

    override val acceptedConnectionProperties: Set<String> = emptySet()

    override fun create(properties: Map<String, String>): Database =
        error("The broken probe never opens a connection.")
}

/** A factory with no connection behind it, shared by the probes above. */
private abstract class StubFactory(override val typeName: String) : DatabaseFactory {

    override val acceptedConnectionProperties: Set<String> = emptySet()

    override fun create(properties: Map<String, String>): Database =
        error("The stub factory never opens a connection.")
}
