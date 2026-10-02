package io.github.heyhey123.skriptorm.impl.mongo.integration

import com.mongodb.internal.build.MongoDriverVersion
import io.github.heyhey123.skriptorm.result.WriteResult
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.junit.jupiter.api.Assumptions
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What the MongoDB server received, counted by the server, beside what the plugin asked for.
 *
 * The counting driver in the benchmarks module sits at the driver boundary, so it sees one `insertMany`
 * call whatever the driver then does with it, and the SQL layer has its own server-side count for the
 * same reason. This is the MongoDB one, the backend CI ran without. `serverStatus().opcounters` counts
 * commands by kind, and `insert` advances once per insert command: a single five-thousand-document
 * `insertMany` moves it by one when the driver sends one bulk command, and by five thousand when it
 * sends them one at a time. Which shape the driver chose is reported, never gated — a change in it is
 * a finding, not a failure.
 *
 * Runs against whichever server the suite resolves — an external one configured through
 * `skriptorm.test.mongo.*`, a container on a machine that has Docker — and aborts with an explanation
 * when there is none.
 */
class MongoServerSideCountIntegrationTest : MongoIntegrationTestBase() {

    @Test
    fun `the server counts the insert commands one insert many produced`() = runBlocking<Unit> {
        recreateTable()
        // The reference size: 5000 rows, the size the SQL cases and baseline.json record.
        val rows = 5000
        val values = (1..rows).map { userValues(name = "user-$it", age = it) }

        // `serverStatus` is a command, and `opcounters.insert` and `opcounters.query` do not count
        // commands, so the probes themselves leave those two deltas alone. The empty block still runs:
        // it is the way a concurrent writer is detected, because the global counters would include its
        // inserts too.
        val control = measure { null }

        // The counters are global to the mongod, so the test must prove they can be read as its own
        // work before trusting them: a nonzero advance during the empty block, or a third client
        // attached, means someone else's operations would be counted beside the plugin's.
        val unattributable = unattributableReason(control)
        if (unattributable != null) {
            report("attribution", "unattributable: $unattributable")
        }

        val measured = measure { queries.insertMany(values).execute(usersTable) }
        val stored = rawCount()

        val insertCommands = delta(control, measured, "insert")
        val queryCommands = delta(control, measured, "query")

        report(
            "probes",
            "alone, around nothing: inserts=${control.span("insert")} queries=${control.span("query")}"
        )
        report(
            "measurement",
            "docs=$rows insertCommandsReceived=$insertCommands queryCommandsReceived=$queryCommands " +
                "storedInCollection=$stored pluginReported=${measured.reported} " +
                "pluginCountExact=${measured.countExact} connectionsCurrent=${measured.after.connections()} " +
                fingerprint()
        )

        // A skip whose reason exists only inside a test report is a skip nobody can explain later, and
        // the run that exposed this layer showed exactly that: one case was skipped and no log said
        // which or why. Every reason is reported on the same channel as the numbers.
        if (unattributable != null) {
            report("skipped", "the counters cannot be attributed: $unattributable")
            Assumptions.assumeTrue(false, "MongoDB server-side count aborted: $unattributable")
        }
        if (insertCommands == null) {
            report("skipped", "this server does not report opcounters.insert, so nothing can be attributed")
            Assumptions.assumeTrue(
                false,
                "this server does not report opcounters.insert, so nothing can be attributed"
            )
        }

        assertEquals(rows.toLong(), stored, "the collection must hold the documents the plugin wrote")
        // Only the floor is asserted. An upper bound tied to the document count is not a mechanism
        // invariant: the shape of the insertMany command belongs to the driver, so a change in it is the
        // finding this layer exists to report and never a failure it may raise.
        val ranInsertCommands =
            requireNotNull(insertCommands) { "the insert command count must have been readable" }
        assertTrue(
            ranInsertCommands >= 1,
            "the server must have received at least one insert command, and it received $ranInsertCommands"
        )
    }

    /**
     * Why the global counters cannot be read as this test's own work, or null when they can.
     *
     * `opcounters` counts every command the mongod received, not just this test's, so the empty control
     * block is the proof of ownership: a nonzero advance means another client wrote while this test was
     * measuring. A third client that is attached but idle would not move the counters yet, so the open
     * connection count is checked too — this JVM opens exactly two clients, the plugin's and the
     * verification client, and each holds one connection when used sequentially.
     */
    private fun unattributableReason(control: Sample): String? {
        // A missing counter is not a concurrent writer: the missing-counter skip handles that. The
        // span being null means `opcounters` is unreadable, so there is nothing to attribute here yet.
        val insertSpan = control.span("insert") ?: return null
        val querySpan = control.span("query") ?: return null
        if (insertSpan != 0L || querySpan != 0L) {
            return "the counters advanced during the empty control block " +
                "(insert +$insertSpan, query +$querySpan), so another client wrote while this test measured"
        }
        val current = control.after.connections()
            ?: return "serverStatus has no connections.current, so a third client cannot be ruled out"
        if (current > OWN_CLIENT_CONNECTIONS) {
            return "$current connection(s) are open but this test's two clients need at most " +
                "$OWN_CLIENT_CONNECTIONS, so a third client is attached and the global counters would " +
                "include its operations too"
        }
        return null
    }

    /** Runs [block] between two readings of the server's own counters. */
    private suspend fun measure(block: suspend () -> WriteResult?): Sample {
        val before = serverStatus()
        val result = block()
        val after = serverStatus()
        return Sample(before, after, result?.affectedCount ?: 0L, result?.countExact ?: true)
    }

    /** The server's own status document. */
    private fun serverStatus(): Document = runCommand(Document("serverStatus", 1))

    /**
     * The environment this number was measured in, on the same line as the number.
     *
     * A count without its environment is not a measurement: the driver's version and its url options
     * decide the batch shape this test exists to report, and the machine decides nothing here but is
     * recorded anyway because a number that travels without one cannot be reproduced.
     */
    private fun fingerprint(): String {
        val url = redactedUrl()
        return "driver=${MongoDriverVersion.NAME}/${MongoDriverVersion.VERSION} " +
            "server=${serverStatus().getString("version") ?: "unknown"} url=$url " +
            "os=${System.getProperty("os.name")} ${System.getProperty("os.version")} " +
            "arch=${System.getProperty("os.arch")} cpu=${cpuModel()} " +
            "cores=${Runtime.getRuntime().availableProcessors()} java=${System.getProperty("java.version")} " +
            "commit=${System.getenv("GITHUB_SHA") ?: "unknown"}"
    }

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

    /**
     * The measured block's own advance on one counter.
     *
     * The formula is the same as the SQL case's, and for MongoDB it simplifies: `serverStatus` is a
     * command, so it advances `opcounters.command` and leaves `opcounters.insert` and `opcounters.query`
     * alone, which makes the probe cost zero and the delta exactly the block's own work.
     */
    private fun delta(control: Sample, measured: Sample, field: String): Long? {
        val before = control.before.opcounter(field) ?: return null
        val after = measured.after.opcounter(field) ?: return null
        val probeCost = (measured.before.opcounter(field) ?: return null) - before
        return after - before - probeCost
    }

    /** The two readings around one measured block, and what the plugin said it did. */
    private data class Sample(
        val before: Document,
        val after: Document,
        val reported: Long,
        val countExact: Boolean
    ) {
        fun span(field: String): Long? {
            val first = before.opcounter(field) ?: return null
            val last = after.opcounter(field) ?: return null
            return last - first
        }
    }

    private companion object {
        /** The two clients this JVM opens: the plugin's and the verification client. */
        const val OWN_CLIENT_CONNECTIONS = 2L
    }
}

/** The value of one `opcounters` field, as a long. */
private fun Document.opcounter(field: String): Long? =
    (get("opcounters", Document::class.java)?.get(field) as? Number)?.toLong()

/** `connections.current`, or null when the server does not report it. */
private fun Document.connections(): Long? =
    (get("connections", Document::class.java)?.get("current") as? Number)?.toLong()
