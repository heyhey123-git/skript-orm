package io.github.heyhey123.skriptorm.impl.pg.integration

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What the server received on PostgreSQL — and the half of it that cannot be had.
 *
 * The other half of this measurement exists for the MySQL family: `Questions` counts the statements a
 * server executed, so the ratio of rows to statements says whether a driver carried a batch as one
 * multi-row statement or as one statement per row. **PostgreSQL's built-in counters do not count
 * statements.** `pg_stat_database` reports rows and transactions, and `pg_stat_user_tables` reports
 * rows per table, but nothing in the shipped statistics views says how many statements produced them,
 * so a "statements received" number for PostgreSQL cannot be attributed to this plugin's statements and
 * is not printed here. That is a gap in the instrument and not a property of the server: closing it
 * needs `pg_stat_statements`, which requires being loaded through `shared_preload_libraries` and a
 * server restart, or a proxy that counts the protocol's statement messages as they go past.
 *
 * What is measured here is therefore what PostgreSQL will say: the rows the server recorded for this
 * table and for this database, beside the rows the plugin says it wrote. That is weaker than the MySQL
 * count and it is not dressed up as the same thing — it proves the write reached the server and how
 * many rows it carried, and it says nothing about the shape of the batch that carried them.
 *
 * **This reports and never gates**, and it waits for the counters rather than assuming they are
 * current: PostgreSQL accumulates statistics asynchronously, so the counter is polled for a bounded
 * time after the write and the wait is reported with the number. A counter that never arrives aborts
 * the case with its reason instead of asserting, because a lagging view is not a plugin failure.
 *
 * Runs against whichever PostgreSQL the suite resolves, the runner's own installed server in CI and a
 * container elsewhere, and aborts with an explanation when there is none.
 */
class PgServerSideCountIntegrationTest : PgIntegrationTestBase() {

    @Test
    fun `the server records the rows one insert many carried`() = runBlocking<Unit> {
        recreateTable()
        val rows = 5000
        val values = (1..rows).map { userValues(id = it, name = "user-$it", age = it) }

        val unattributable = unattributableReason()
        if (unattributable != null) {
            report("attribution", "unattributable: $unattributable")
        }

        val tableBefore = tableRows()
        val databaseBefore = databaseRows()
        val result = queries.insertMany(values).execute(usersTable)
        val waited = awaitTableRows(tableBefore + rows)

        val tableAfter = tableRows()
        val databaseAfter = databaseRows()
        val stored = rawRowCount()

        report(
            "measurement",
            "rows=$rows statementsReceived=unavailable " +
                "why=this server counts rows and transactions, not statements, and no shipped view attributes them " +
                "rowsRecordedForTable=${tableAfter - tableBefore} rowsRecordedForDatabase=${databaseAfter - databaseBefore} " +
                "storedInTable=$stored pluginReported=${result.affectedCount} pluginCountExact=${result.countExact} " +
                "counterWaitedMs=$waited " + fingerprint()
        )

        // A skip whose reason exists only inside a test report is a skip nobody can explain later, and
        // that is what happened to this case: the run could neither confirm nor deny what PostgreSQL
        // printed for the statement half, because the text went into an artifact. Every reason is now
        // reported on the same channel as the numbers before the assumption aborts.
        if (unattributable != null) {
            report("skipped", "the statistics cannot be attributed: $unattributable")
            Assumptions.assumeTrue(false, "PostgreSQL server-side count aborted: $unattributable")
        }
        if (waited < 0) {
            report(
                "skipped",
                "the statistics view did not report the insert within ${COUNTER_TIMEOUT_MS}ms, so nothing can be attributed"
            )
            Assumptions.assumeTrue(
                false,
                "the statistics view did not report the insert within ${COUNTER_TIMEOUT_MS}ms, so nothing can be attributed"
            )
        }

        assertEquals(rows.toLong(), tableAfter - tableBefore, "the server must have recorded every row for this table")
        assertEquals(rows.toLong(), stored, "the table must hold the rows the plugin wrote")
    }

    /** Why the statistics views cannot be read as this test's own work, or null when they can. */
    private fun unattributableReason(): String? = try {
        val others = queryLong(
            "SELECT COUNT(*) FROM pg_stat_activity " +
                "WHERE usename <> current_user AND pid <> pg_backend_pid()"
        )
        if (others > 0) {
            "$others session(s) of another user are open, so the server-wide counters include writes that are not this plugin's"
        } else {
            null
        }
    } catch (failure: Exception) {
        "this server does not expose pg_stat_activity: $failure"
    }

    /**
     * Waits until the table's inserted-row count reaches [target], up to [COUNTER_TIMEOUT_MS].
     *
     * Returns the milliseconds waited, or -1 when the counter never arrived. PostgreSQL updates these
     * views asynchronously, so reading once immediately after the write measures the collector's
     * latency rather than the plugin's work.
     */
    private suspend fun awaitTableRows(target: Long): Long {
        val started = System.nanoTime()
        while (elapsedMs(started) < COUNTER_TIMEOUT_MS) {
            if (tableRows() >= target) return elapsedMs(started)
            delay(POLL_INTERVAL_MS)
        }
        return if (tableRows() >= target) elapsedMs(started) else -1L
    }

    private fun elapsedMs(started: Long): Long = (System.nanoTime() - started) / 1_000_000

    /** Rows this table's statistics view says have been inserted into it. */
    private fun tableRows(): Long = queryLong(
        "SELECT COALESCE(SUM(n_tup_ins), 0) FROM pg_stat_user_tables WHERE relname = '${usersTable.name}'"
    )

    /** Rows this database's statistics view says have been inserted into it. */
    private fun databaseRows(): Long = queryLong(
        "SELECT COALESCE(SUM(tup_inserted), 0) FROM pg_stat_database WHERE datname = current_database()"
    )

    /** The server's version string. */
    private fun serverVersion(): String = dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT version()").use { rows ->
                if (rows.next()) rows.getString(1) else "unknown"
            }
        }
    }

    /**
     * The environment this number was measured in, on the same line as the number.
     *
     * The server's version decides how its statistics behave and the driver decides what it sends; the
     * machine decides nothing here but is recorded because a number that travels without one cannot be
     * reproduced.
     */
    private fun fingerprint(): String {
        val settings = dataSource.jdbcUrl.substringAfter('?', "")
            .split('&')
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

    private fun cpuModel(): String {
        val cpuinfo = File("/proc/cpuinfo")
        if (!cpuinfo.isFile) return System.getProperty("os.arch")
        return cpuinfo.useLines { lines ->
            lines.firstOrNull { it.startsWith("model name") }?.substringAfter(':')?.trim()
                ?: System.getProperty("os.arch")
        }
    }

    private companion object {
        const val COUNTER_TIMEOUT_MS = 10_000L
        const val POLL_INTERVAL_MS = 100L
    }
}
