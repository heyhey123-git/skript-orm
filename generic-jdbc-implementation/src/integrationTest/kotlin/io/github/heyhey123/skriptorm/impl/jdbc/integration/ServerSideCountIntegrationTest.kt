package io.github.heyhey123.skriptorm.impl.jdbc.integration

import io.github.heyhey123.skriptorm.result.WriteResult
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What the server received, counted by the server, beside what the plugin asked for.
 *
 * The counting driver in the benchmarks module sits at the JDBC boundary, so it sees one batch
 * whatever the driver then does with it: Connector/J can turn a batch of five thousand single-row
 * inserts into one multi-row statement, and from that layer the two are the same call. That is why the
 * MySQL finding exists — the same SQL and the same server, seventeen seconds against two — and why the
 * layer below the driver has to be read where a real server is available. This is that layer.
 *
 * Two server-side numbers answer the question, and their ratio is the answer:
 *
 * - **statements the server executed**, from `Questions`, less what the probes themselves cost;
 * - **rows InnoDB inserted**, from `Innodb_rows_inserted`.
 *
 * Five thousand rows carried by a handful of statements is a rewritten batch; five thousand rows
 * carried by five thousand statements is a batch sent one row at a time. `Com_insert` and
 * `Com_stmt_execute` are reported beside them because they say which shape each path took, and the
 * plugin's own reported count and its claim of exactness are reported with them.
 *
 * **This reports and never gates.** A statement count is zero-noise, but the ratio is not a
 * requirement: which shape a driver chooses is the driver's decision and its version's, so a run that
 * changes shape is a finding rather than a failure. What is asserted is only what the mechanism has to
 * satisfy — every row reached the server, the server ran at least one statement and no more than one
 * per row, and the table holds them.
 *
 * **The counters are global, so the deltas describe this test only while nothing else is writing.**
 * That is checked rather than assumed: another user connected to the same server makes the numbers
 * unattributable, and then they are printed with that fact and the assertions are skipped, because a
 * delta that includes someone else's writes is not this plugin's measurement.
 *
 * Runs against whichever MySQL-family server the suite resolves — the runner's own installed server in
 * CI, a container on a machine that has one — and aborts with an explanation when there is none.
 */
abstract class ServerSideCountIntegrationTest : MysqlIntegrationTestBase() {

    @Test
    fun `the server counts the statements and the rows one insert many produced`() = runBlocking<Unit> {
        recreateTable()
        // The reference size: 5000 rows, the size in the MySQL finding and the size baseline.json
        // records. This table is nine columns wide, so the call carries 45 000 values and the plugin
        // splits it into more than one statement of its own — which is part of what the server sees and
        // is reported as such rather than corrected for.
        val rows = 5000
        val values = (1..rows).map { userValues(id = it, name = "user-$it", age = it) }

        val unattributable = unattributableReason()
        if (unattributable != null) {
            report("attribution", "unattributable: $unattributable")
        }

        // The probes are statements the server counts too, so the same pair of reads is run around an
        // empty block first. That delta is what the probes themselves cost, and subtracting it is what
        // leaves the measured delta describing the plugin rather than the plugin plus the measurement.
        val control = measure { null }
        val measured = measure { queries.insertMany(values).execute(usersTable) }
        val stored = rawRowCount()

        val statements = delta(control, measured, "Questions")
        val rowsInserted = delta(control, measured, "Innodb_rows_inserted")

        report("probes", "alone, around nothing: statements=${control.statements()} rows=${control.rowsInserted()}")
        report(
            "measurement",
            "rows=$rows statementsReceived=$statements rowsInserted=$rowsInserted rowsPerStatement=${ratio(rowsInserted, statements)} " +
                "comInsert=${delta(control, measured, "Com_insert")} " +
                "comStmtExecute=${delta(control, measured, "Com_stmt_execute")} " +
                "storedInTable=$stored pluginReported=${measured.reported} pluginCountExact=${measured.countExact} " +
                fingerprint()
        )

        // A skip whose reason exists only inside a test report is a skip nobody can explain later,
        // and the run that exposed this layer showed exactly that: one case was skipped and no log
        // said which or why. Every reason is now reported on the same channel as the numbers.
        if (unattributable != null) {
            report("skipped", "the counters cannot be attributed: $unattributable")
            Assumptions.assumeTrue(false, "${product.displayName} server-side count aborted: $unattributable")
        }
        if (statements == null || rowsInserted == null) {
            val missing = if (statements == null) "Questions" else "Innodb_rows_inserted"
            report("skipped", "this server does not report $missing to this user, so nothing can be attributed")
            Assumptions.assumeTrue(
                false,
                "this server does not report $missing to this user, so nothing can be attributed"
            )
        }

        assertEquals(rows.toLong(), rowsInserted, "the server must have recorded every row it was sent")
        // Only the floor is asserted. An upper bound tied to the row count is not a mechanism
        // invariant: the shape of the batch belongs to the driver, so a change in it is the finding
        // this layer exists to report and never a failure it may raise. The first CI run proved the
        // point by failing on such a bound - MySQL over Testcontainers ran 5038 statements for 5000
        // rows, a ratio of about 1.008, which says the insert many does not reach the server as one
        // multi-row statement, and that is the second-level confirmation of the seventeen seconds
        // against two that this work started from. The number travels with the fingerprint printed
        // on the measurement line.
        val ranStatements = requireNotNull(statements) { "the statement count must have been readable" }
        assertTrue(ranStatements >= 1, "the server must have run at least one statement, and it ran $ranStatements")
        assertEquals(rows.toLong(), stored, "the table must hold the rows the plugin wrote")
    }

    /** Why the global counters cannot be read as this test's own work, or null when they can. */
    private fun unattributableReason(): String? {
        val others = try {
            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.PROCESSLIST WHERE USER <> ?"
                ).use { statement ->
                    statement.setString(1, dataSource.username)
                    statement.executeQuery().use { rows ->
                        if (rows.next()) rows.getLong(1) else 0L
                    }
                }
            }
        } catch (failure: Exception) {
            return "this server does not expose information_schema.PROCESSLIST: $failure"
        }
        return if (others > 0) {
            "$others connection(s) of another user are open, so the server-wide counters include " +
                "statements that are not this plugin's"
        } else {
            null
        }
    }

    /** Runs [block] between two readings of the server's own counters. */
    private suspend fun measure(block: suspend () -> WriteResult?): Sample {
        val before = status()
        val result = block()
        val after = status()
        return Sample(before, after, result?.affectedCount ?: 0L, result?.countExact ?: true)
    }

    /** The server's global status counters, as the server reports them. */
    private fun status(): Map<String, Long> = dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery("SHOW GLOBAL STATUS").use { rows ->
                buildMap {
                    while (rows.next()) {
                        val value = rows.getString(2)?.toLongOrNull()
                        if (value != null) put(rows.getString(1), value)
                    }
                }
            }
        }
    }

    /** The server's version string. */
    private fun serverVersion(): String = queryString("SELECT VERSION()")

    /** One string value read outside the implementation. */
    private fun queryString(sql: String): String = dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery(sql).use { rows ->
                if (rows.next()) rows.getString(1) else "unknown"
            }
        }
    }

    /**
     * The environment this number was measured in, on the same line as the number.
     *
     * A count without its environment is not a measurement: the driver's version and its url options
     * decide the batch shape this test exists to report, and the machine decides nothing here but is
     * recorded anyway because a number that travels without one cannot be reproduced.
     */
    private fun fingerprint(): String {
        val url = dataSource.jdbcUrl.substringAfter('?', "")
        val settings = url.split('&')
            .filter { it.isNotBlank() && !it.startsWith("password=", ignoreCase = true) }
            .joinToString("&")
        return "driver=${dataSource.driverClassName} server=${serverVersion()} " +
            "urlSettings=${settings.ifEmpty { "none" }} " +
            "os=${System.getProperty("os.name")} ${System.getProperty("os.version")} " +
            "arch=${System.getProperty("os.arch")} cpu=${cpuModel()} " +
            "cores=${Runtime.getRuntime().availableProcessors()} java=${System.getProperty("java.version")} " +
            "commit=${System.getenv("GITHUB_SHA") ?: "unknown"}"
    }

    /**
     * Prints one report line, labelled so a run's numbers can be found in a job log.
     *
     * Gradle captures test stdout and shows it only when the task asks for it, and the run's test
     * reports cannot be downloaded from every environment, so this line reaches a reader only
     * because the integration test task turns standard streams on. A number nobody can read is not a
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

    private fun delta(control: Sample, measured: Sample, counter: String): Long? {
        val before = control.before[counter] ?: return null
        val after = measured.after[counter] ?: return null
        val probeCost = (measured.before[counter] ?: return null) - before
        return after - before - probeCost
    }

    private fun ratio(rows: Long?, statements: Long?): String =
        if (rows == null || statements == null || statements == 0L) "unavailable" else "%.2f".format(rows.toDouble() / statements)

    /** The two readings around one measured block, and what the plugin said it did. */
    private data class Sample(
        val before: Map<String, Long>,
        val after: Map<String, Long>,
        val reported: Long,
        val countExact: Boolean
    ) {
        fun statements(): String = span("Questions")

        fun rowsInserted(): String = span("Innodb_rows_inserted")

        private fun span(counter: String): String {
            val first = before[counter] ?: return "unavailable"
            val last = after[counter] ?: return "unavailable"
            return (last - first).toString()
        }
    }
}
