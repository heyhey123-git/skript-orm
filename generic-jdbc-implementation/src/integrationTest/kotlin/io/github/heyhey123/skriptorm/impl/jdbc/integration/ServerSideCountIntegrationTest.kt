package io.github.heyhey123.skriptorm.impl.jdbc.integration

import io.github.heyhey123.skriptorm.result.WriteResult
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Compares the rows inserted with the statements counted by a MySQL or MariaDB server.
 *
 * The benchmark counting driver sees calls before the JDBC driver sends SQL. These server-side
 * counters show whether the driver rewrote a batch into multi-row statements:
 * `Questions` counts statements, and `Innodb_rows_inserted` counts inserted rows. The test also
 * reports `Com_insert`, `Com_stmt_execute`, and the addon's own affected-row count.
 *
 * The statement-to-row ratio is diagnostic because batch rewriting varies by driver and version.
 * Assertions check that the server inserted every row, executed at least one statement, and retained
 * the rows in the table.
 *
 * The counters are global. If another user is connected, their activity could affect the result;
 * the test reports that condition and skips the assertions.
 */
abstract class ServerSideCountIntegrationTest : MysqlIntegrationTestBase() {

    @Test
    fun `the server counts the statements and the rows one insert many produced`() = runBlocking<Unit> {
        recreateTable()
        // Match the 5000-row benchmark. The nine-column table produces 45,000 bound values, so
        // the addon may split the batch into multiple statements before the driver receives it.
        val rows = 5000
        val values = (1..rows).map { userValues(id = it, name = "user-$it", age = it) }

        val unattributable = unattributableReason()
        if (unattributable != null) {
            report("attribution", "unattributable: $unattributable")
        }

        // Measure the counter queries around an empty block, then subtract that probe cost.
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

        // Print skip reasons alongside the measurements so they remain visible in CI logs.
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
        // The driver controls batch shape, so there is no upper bound on statement count here.
        val ranStatements = requireNotNull(statements) { "the statement count must have been readable" }
        assertTrue(ranStatements >= 1, "the server must have run at least one statement, and it ran $ranStatements")
        assertEquals(rows.toLong(), stored, "the table must hold the rows the plugin wrote")
    }

    /** Returns why the global counters cannot be attributed to this test, or null if they can. */
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
     * Records the server, driver settings, and host alongside each measurement so results can be
     * compared across runs.
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
     * Prints a labelled measurement line to the integration test log.
     */
    private fun report(label: String, message: String) {
        println("[server-side] $label: $message")
    }

    /**
     * Reads the CPU model from `/proc/cpuinfo` when available, falling back to the architecture.
     * This value is reported for context and is never asserted.
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
