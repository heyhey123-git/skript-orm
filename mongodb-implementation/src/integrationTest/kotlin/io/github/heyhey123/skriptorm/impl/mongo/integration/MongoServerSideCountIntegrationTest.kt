package io.github.heyhey123.skriptorm.impl.mongo.integration

import com.mongodb.internal.build.MongoDriverVersion
import kotlinx.coroutines.runBlocking
import org.bson.Document
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * How many rows MongoDB holds after one `insert many`, counted by the server.
 *
 * The quantity that matters is rows, and it is also the one that needs no guard. A command count would:
 * the counters that publish it — `serverStatus().opcounters`, or `metrics.document` — are global to the
 * mongod, so the number includes whatever any other client did inside the window, and this test would
 * first have to prove it was alone before the number could be read as its own work. That proof is what a
 * shared test JVM makes fail rather than hold: other test classes in the same worker keep connection
 * pools open, the open-connection count never falls to this test's own two, and the case aborted as
 * unattributable on every run instead of measuring anything — which is exactly what CI showed.
 *
 * The collection's own document count has no such problem. It is per-collection by construction: it is
 * this test's collection, so the server is answering about this test's work and nothing else, and there
 * is no third client to rule out and therefore no guard to satisfy. The count is taken through the
 * verification client rather than the implementation, so it is MongoDB's answer about its own store
 * rather than the plugin agreeing with itself. What the plugin said it wrote is printed beside it and
 * not asserted, because the plugin's count is its own claim.
 *
 * Runs against whichever server the suite resolves — an external one configured through
 * `skriptorm.test.mongo.*`, a container on a machine that has Docker — and aborts with an explanation
 * when there is none.
 */
class MongoServerSideCountIntegrationTest : MongoIntegrationTestBase() {

    @Test
    fun `the server holds the rows one insert many produced`() = runBlocking<Unit> {
        recreateTable()
        // Use the same 5000-row batch size as the SQL tests and insert benchmark.
        val rows = 5000
        val values = (1..rows).map { userValues(name = "user-$it", age = it) }

        val result = queries.insertMany(values).execute(usersTable)
        val insertedRows = rawCount()

        report(
            "measurement",
            "docs=$rows insertedRows=$insertedRows " +
                "pluginReported=${result.affectedCount} " +
                "pluginCountExact=${result.countExact} " +
                fingerprint()
        )

        // The mechanism invariant, and the only assertion: every row the plugin was handed is in the
        // collection. The shape of the command the driver chose to send is asserted nowhere, because that
        // belongs to the driver, and a change in it is a finding this layer reports rather than a failure
        // it may raise.
        assertEquals(rows.toLong(), insertedRows, "the collection must hold the rows the plugin inserted")
    }

    /**
     * The environment this number was measured in, on the same line as the number.
     *
     * A count without its environment is not a measurement: the driver's version and its url options
     * decide the batch shape a reader may want to reason about, and the machine decides nothing here but
     * is recorded anyway because a number that travels without one cannot be reproduced.
     */
    private fun fingerprint(): String {
        val url = redactedUrl()
        return "driver=${MongoDriverVersion.NAME}/${MongoDriverVersion.VERSION} " +
            "server=${serverVersion()} url=$url " +
            "os=${System.getProperty("os.name")} ${System.getProperty("os.version")} " +
            "arch=${System.getProperty("os.arch")} cpu=${cpuModel()} " +
            "cores=${Runtime.getRuntime().availableProcessors()} java=${System.getProperty("java.version")} " +
            "commit=${System.getenv("GITHUB_SHA") ?: "unknown"}"
    }

    /**
     * The server's version, asked for as metadata rather than measured.
     *
     * `buildInfo` is read rather than `serverStatus` so that no global counter appears in this test at
     * all: the version describes the server, it does not count its work, and nothing here depends on who
     * else is connected.
     */
    private fun serverVersion(): String =
        runCommand(Document("buildInfo", 1)).getString("version") ?: "unknown"

    /** The endpoint url with any password removed, so a shared log does not leak a credential. */
    private fun redactedUrl(): String {
        var url = MongoTestServer.requireEndpoint().url
        url = url.replace(Regex("password=[^&]*", RegexOption.IGNORE_CASE), "password=<hidden>")
        val scheme = url.substringBefore("://", "")
        val rest = url.substringAfter("://", url)
        val at = rest.lastIndexOf('@')
        if (at >= 0) {
            val authority = rest.substring(0, at)
            val colon = authority.lastIndexOf(':')
            if (colon >= 0) {
                url = "$scheme://${authority.substring(0, colon + 1)}<hidden>@${rest.substring(at + 1)}"
            }
        }
        return url
    }

    /**
     * Prints one report line, labelled so a run's numbers can be found in a job log.
     *
     * Gradle captures test stdout and shows it only when the task asks for it, and the run's test
     * reports cannot be downloaded from every environment, so this line reaches a reader only because
     * the integration test task turns standard streams on. A number nobody can read is not a
     * measurement, and this layer exists to be read.
     */
    private fun report(label: String, message: String) {
        println("[server-side] $label: $message")
    }

    /**
     * The CPU model, read where the machine publishes it.
     *
     * Java has no portable way to ask, and the runner's CPU is the difference between one nightly
     * number and the next, so `/proc/cpuinfo` is read when it exists and the architecture is used when
     * it does not. The value is descriptive only: nothing compares it.
     */
    private fun cpuModel(): String {
        val cpuinfo = File("/proc/cpuinfo")
        if (!cpuinfo.isFile) return System.getProperty("os.arch")
        return cpuinfo.useLines { lines ->
            lines.firstOrNull { it.startsWith("model name") }
                ?.substringAfter(':')
                ?.trim()
                ?: System.getProperty("os.arch")
        }
    }
}
