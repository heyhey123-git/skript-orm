package buildlogic.tasks

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class ReportSkriptServerBenchmark : DefaultTask() {

    /** The log of the server that just ran. */
    @get:Internal
    abstract val serverLog: RegularFileProperty

    /** Where the medians are written as JSON for the CI job to record into the gh-pages history. */
    @get:Internal
    abstract val resultsFile: RegularFileProperty

    @get:Input
    abstract val backend: Property<String>

    @TaskAction
    fun report() {
        val log = serverLog.get().asFile
        if (!log.isFile) {
            throw GradleException("The benchmark server wrote no log at ${log.absolutePath}.")
        }
        val lines = log.readLines()

        val failures = lines.filter { "SKRIPTORM_BENCH=FAIL" in it }
        if (lines.none { "SKRIPTORM_BENCH=DONE" in it }) {
            throw GradleException("The benchmark did not report completion.")
        }
        if (failures.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("The Skript benchmark could not measure what it was asked to:")
                    failures.forEach { appendLine("  - ${it.substringAfter("SKRIPTORM_BENCH=").trim()}") }
                }
            )
        }

        val loaded = lines.firstOrNull { "Enabling skript-orm" in it }
            ?: throw GradleException(
                "The benchmark server never enabled the plugin under test, so nothing in this run says " +
                    "anything about this plugin. Its log is at ${log.absolutePath}."
            )

        val allMeasurements = lines.mapNotNull { parse(it) }
        val measurements = allMeasurements.filter { it.kind in setOf("write", "read", "warmwrite", "warmread") }
        val comparisons = allMeasurements - measurements.toSet()
        if (measurements.none { it.kind == "write" }) {
            throw GradleException(
                buildString {
                    appendLine("The benchmark server ran but measured no write, so there is nothing to report.")
                    appendLine("Its log is at ${log.absolutePath}.")
                    // The scripts say what they handed each measurement, so a loop that produced nothing is
                    // reported rather than guessed at from ninety measurements that all say `<none>`.
                    val diagnostics = lines.filter { "SKRIPTORM_BENCH=DIAG" in it }
                    if (diagnostics.isNotEmpty()) {
                        appendLine("What the scripts said they were measuring:")
                        diagnostics.take(12).forEach {
                            appendLine("  ${it.substringAfter("SKRIPTORM_BENCH=").trim()}")
                        }
                    }
                }
            )
        }
        val unreadable = allMeasurements.filter { it.wall == null || it.gap == null }
        if (unreadable.isNotEmpty()) {
            throw GradleException("The benchmark logged unreadable wallNs or gapNs values: ${unreadable.joinToString { "${it.kind} ${it.rows}" }}")
        }
        for (measurement in allMeasurements) {
            if (measurement.kind.endsWith("write")) {
                require(measurement.affected == measurement.rows) {
                    "${measurement.kind} ${measurement.rows}: affected row count was ${measurement.affected}."
                }
            } else {
                val refused = measurement.rows > 5000
                val expected = if (refused) 0 else measurement.rows
                require(measurement.stored == expected) {
                    "${measurement.kind} ${measurement.rows}: stored ${measurement.stored}, expected $expected."
                }
                if (refused) {
                    require(measurement.error?.startsWith("select many read more than 5000 rows") == true)
                } else {
                    require(measurement.error == null) {
                        "${measurement.kind} failed: ${measurement.error}"
                    }
                }
            }
        }

        // The scripts count the whole table, because nothing in the benchmark deletes: the rows a
        // measurement added are the difference from the line before it. That difference is also what says
        // the statement did what it claims — a write it did not want to grow the table by its own size, a
        // read it did not want to grow the table at all — which is why a disagreement fails the run. A
        // number that measures a write that did not happen is not a measurement, however it is printed.
        // The count is cumulative, so a measurement's own rows are the difference from the line before it.
        // The accumulator is the previous count rather than the previous difference, which an earlier
        // version of this got wrong in a way that made every difference null and the check below vacuous.
        var counted: Int? = 0
        val added = measurements.map { measurement ->
            val delta = if (counted == null || measurement.total == null) null else measurement.total - counted
            counted = measurement.total ?: counted
            delta
        }
        val short = measurements.zip(added).filter { (measurement, delta) ->
            val expected = if (measurement.kind.endsWith("write")) measurement.rows else 0
            delta != null && delta != expected
        }
        if (short.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("The table did not grow by what a measurement says it did, so what it timed is not what it says:")
                    short.forEach { (measurement, delta) ->
                        val expected = if (measurement.kind.endsWith("write")) "${measurement.rows} rows" else "no rows"
                        appendLine("  - ${measurement.kind} ${measurement.rows}: expected $expected added, the table grew by $delta")
                    }
                }
            )
        }

        val bootSeconds = lines.firstNotNullOfOrNull { line ->
            Regex("""Done \(([0-9.]+)s\)!""").find(line)?.groupValues?.get(1)
        }
        logger.lifecycle("The benchmark server enabled {}, and booted in {}s.", loaded.trim(), bootSeconds ?: "?")
        logger.lifecycle("Database backend: {}", backend.get())

        // An idle window and counted spin loops check the tick observer.
        val calibrations = lines.mapNotNull { parseCalibration(it) }
        if (calibrations.isNotEmpty()) {
            logger.lifecycle("")
            logger.lifecycle("Controls, whose size the script chose instead of measuring:")
            logger.lifecycle("  control       spun       spin       window   longest tick     overrun")
            calibrations.forEach { control ->
                val overrun = control.gap?.let { (it - TICK_MILLIS).coerceAtLeast(0.0) }
                logger.lifecycle(
                    "  %-11s %6s %10s %12s %14s %10s".format(
                        control.label,
                        control.spun?.toString() ?: "-",
                        milliseconds(control.spin, control.spinRaw),
                        milliseconds(control.window, null),
                        milliseconds(control.gap, control.gapRaw),
                        overrun?.let { "%.2f ms".format(it) } ?: "unreadable"
                    )
                )
            }
            logger.lifecycle("The tick gap includes observer overhead and other server work. A zero overrun does not mean zero main-thread cost.")
        }

        logger.lifecycle("")
        logger.lifecycle("One Skript statement, as the script that issued it saw it:")
        logger.lifecycle(
            "  kind      rows  ticks   wall clock  longest tick   overrun  wall us/row  overrun us/row          " +
                "plugin        total   added  stored"
        )
        measurements.zip(added).forEach { (measurement, delta) -> logger.lifecycle(format(measurement, delta)) }

        logger.lifecycle("")
        logger.lifecycle("Per size, taken as the middle of what this run measured:")
        allMeasurements
            .groupBy { it.kind to it.rows }
            .toSortedMap(compareBy({ it.first }, { it.second }))
            .forEach { (size, group) -> logger.lifecycle(formatMedian(size.first, size.second, group)) }

        // The same medians as JSON, so the CI job can record them per CPU into the gh-pages history beside
        // the JMH numbers. The tick numbers are report-only and never gated — the reason lives in the
        // workflow where the recording happens — so this file is a record, not a verdict. One named point
        // per (kind, rows) rather than a single headline, because the harness emits a curve and a curve is
        // what gets recorded; a run that reports zero overhang keeps its zero.
        val results = resultsFile.get().asFile
        results.parentFile.mkdirs()
        fun medianValue(group: List<Measurement>, select: (Measurement) -> String?): Long? {
            val values = group.mapNotNull { nanoseconds(select(it)) }.sorted()
            return if (values.isEmpty()) null else values[values.size / 2]
        }
        val tickEntries = mutableListOf<String>()
        allMeasurements
            .groupBy { it.kind to it.rows }
            .toSortedMap(compareBy({ it.first }, { it.second }))
            .forEach { (size, group) ->
                val (kind, rows) = size
                medianValue(group) { it.wallRaw }?.let { wall ->
                    tickEntries += """{"name":"$kind ${rows}rows wall","unit":"ns","value":$wall}"""
                }
                medianValue(group) { it.gapRaw }?.let { gap ->
                    val overrun = (gap - 50_000_000L).coerceAtLeast(0L)
                    tickEntries += """{"name":"$kind ${rows}rows gap","unit":"ns","value":$gap}"""
                    tickEntries += """{"name":"$kind ${rows}rows overrun","unit":"ns","value":$overrun}"""
                }
            }
        recordPipelineSamples(lines, results, tickEntries)
        results.writeText("[${tickEntries.joinToString(",")}]\n")
        logger.lifecycle("Recorded {} tick points to {}.", tickEntries.size, results.absolutePath)
        comparisons.forEach { logger.lifecycle(format(it, null)) }
        lines.filter { "SKRIPTORM_BENCH=SKIP" in it }.forEach { logger.lifecycle(it) }

        logger.lifecycle("")
        logger.lifecycle("Wall time includes asynchronous database work and the wait to resume the trigger. Tick overrun is the longest tick-start gap minus 50 ms; it cannot measure work inside a normal tick.")

        val disagreed = measurements.zip(added)
            .filter { (measurement, delta) -> measurement.affected != null && delta != null && measurement.affected != delta }
        if (disagreed.isNotEmpty()) {
            logger.lifecycle("")
            logger.lifecycle("The plugin's own count differed from the table for {} measurement(s):", disagreed.size)
            disagreed.forEach { (measurement, delta) ->
                logger.lifecycle(
                    "  {} {}: the plugin said {} and the table grew by {}",
                    measurement.kind,
                    measurement.rows,
                    measurement.affected,
                    delta
                )
            }
        }

        logger.lifecycle("")
        logger.lifecycle("What the scripts wrote, unparsed, so the numbers above can be checked against it:")
        lines.filter {
            "SKRIPTORM_BENCH write:" in it ||
                "SKRIPTORM_BENCH read:" in it ||
                "SKRIPTORM_BENCH warmwrite:" in it ||
                "SKRIPTORM_BENCH warmread:" in it ||
                "SKRIPTORM_BENCH calibrate:" in it
        }
            .forEach { line -> logger.lifecycle("  {}", line.substringAfter("SKRIPTORM_BENCH").trim()) }
    }

    /** Keep every sample; warmups are excluded from summaries, not discarded. */
    private fun recordPipelineSamples(lines: List<String>, results: java.io.File, tickEntries: MutableList<String>) {
        val dimensions = listOf("workload", "source", "target", "replaced", "rows")
        val timers = listOf(
            "wallNs", "gapNs", "mainNs", "mainTickMaxNs", "prepareMainNs", "prepareAsyncNs",
            "conversionMainNs", "resultMainNs", "resultAsyncNs", "executionNs", "queueWaitNs", "largestConversionNs"
        )
        val numbers = timers + listOf("sample", "rows", "ticks", "operationId", "syncConversions", "affected", "stored", "total")
        val samples = lines.filter { "SKRIPTORM_BENCH=SAMPLE" in it }.map { line ->
            val kind = Regex("SAMPLE (write|read):").find(line)?.groupValues?.get(1)
                ?: throw GradleException("Invalid pipeline sample: $line")
            val sample = linkedMapOf<String, Any>("kind" to kind)
            for (dimension in dimensions) {
                sample[dimension] = field(line, dimension)
                    ?: throw GradleException("Missing $dimension: $line")
            }
            for (number in numbers) {
                val value = nanoseconds(field(line, number))
                if (value != null) sample[number] = value
            }
            require(timers.all { sample[it] is Long && (sample[it] as Long) >= 0 }) {
                "Missing or invalid phase timings: $line"
            }
            require((sample["operationId"] as? Long ?: 0) > 0) { "Benchmark instrumentation is disabled: $line" }
            require(sample["sample"] is Long) { "Invalid sample number: $line" }
            val count = if (kind == "write") "affected" else "stored"
            require(sample[count] == sample["rows"]) { "Incomplete pipeline operation: $line" }
            if (kind == "read") {
                val error = Regex("error=(.*)$").find(line)?.groupValues?.get(1)?.trim()
                require(error == "<none>" || error.isNullOrEmpty()) { "Pipeline read failed: $line" }
            }
            sample
        }
        require(samples.isNotEmpty()) { "The server recorded no pipeline samples." }
        val groups = samples.filter { (it.getValue("sample") as Long) > 0 }
            .groupBy { sample -> (listOf("kind") + dimensions).joinToString(" ") { sample.getValue(it).toString() } }
        require(groups.size == 34 && samples.size == 442) {
            "Incomplete pipeline matrix: ${groups.size} groups and ${samples.size} samples; expected 34 and 442."
        }
        require(samples.map { it["operationId"] }.toSet().size == samples.size) {
            "Two samples reference the same operation timing; a snapshot was captured too late or the operation did not run."
        }
        val summaries = groups.map { (name, group) ->
            require(group.size == 10 && group.map { it["sample"] }.toSet() == (1L..10L).toSet()) {
                "Expected samples 1 through 10 for $name; received ${group.map { it["sample"] }}"
            }
            val summary = linkedMapOf<String, Any>("name" to name, "samples" to group.size)
            for (timer in timers) {
                val sorted = group.map { it.getValue(timer) as Long }.sorted()
                val median = (sorted[4] / 2.0 + sorted[5] / 2.0).toLong()
                val p95 = sorted[Math.ceil(sorted.size * 0.95).toInt() - 1]
                summary[timer] = mapOf("median" to median, "p95" to p95, "max" to sorted.last())
                tickEntries += JsonOutput.toJson(mapOf("name" to "$name $timer median", "unit" to "ns", "value" to median))
            }
            summary
        }
        val output = results.resolveSibling("pipeline-results.json")
        output.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(mapOf("backend" to backend.get(), "samples" to samples, "summaries" to summaries))) + "\n")
        logger.lifecycle("Recorded {} pipeline samples and {} groups to {}.", samples.size, summaries.size, output)
    }

    private fun format(measurement: Measurement, added: Int?): String {
        val values = measurement.rows
        val overrun = measurement.gap?.let { (it - TICK_MILLIS).coerceAtLeast(0.0) }
        return "  " + listOf(
            measurement.kind,
            "%6d rows".format(measurement.rows),
            "%4d ticks".format(measurement.ticks),
            measurement.wall?.let { "%9.6f ms".format(it) } ?: "%9s".format(describe(measurement.wallRaw)),
            measurement.gap?.let { "%9.6f ms".format(it) } ?: "%9s".format(describe(measurement.gapRaw)),
            overrun?.let { "%8.6f ms".format(it) } ?: "",
            measurement.wall?.let { "%11.3f".format(it * 1000.0 / values) } ?: "",
            overrun?.let { "%13.3f".format(it * 1000.0 / values) } ?: "",
            measurement.affected?.let { "affected %6d".format(it) } ?: "",
            measurement.total?.let { "%8d".format(it) } ?: "",
            added?.let { "%+7d".format(it) } ?: "",
            measurement.stored?.let { "stored %6d".format(it) } ?: "",
            measurement.error?.let { "error: $it" } ?: ""
        ).filter { it.isNotEmpty() }.joinToString("  ")
    }

    private fun formatMedian(kind: String, rows: Int, group: List<Measurement>): String {
        fun median(select: (Measurement) -> Double?): String {
            val values = group.mapNotNull(select).sorted()
            return if (values.isEmpty()) "unparsed" else "%9.6f".format(values[values.size / 2])
        }
        val ticks = group.map { it.ticks }.sorted()
        val affected = group.mapNotNull { it.affected }.sorted()
        val values = rows
        return "  %-9s %6d rows  wall %s ms  longest tick %s ms  ticks %3d  wall %8.3f us/row  overrun %8.3f us/row  %s".format(
            kind,
            rows,
            median { it.wall },
            median { it.gap },
            ticks[ticks.size / 2],
            group.mapNotNull { it.wall }.sorted().let { if (it.isEmpty()) Double.NaN else it[it.size / 2] * 1000.0 / values },
            group.mapNotNull { it.gap }.sorted().let {
                if (it.isEmpty()) Double.NaN else (it[it.size / 2] - TICK_MILLIS).coerceAtLeast(0.0) * 1000.0 / values
            },
            if (affected.isEmpty()) "" else "affected ${affected[affected.size / 2]}"
        )
    }

    private fun parseCalibration(line: String): Calibration? {
        if ("SKRIPTORM_BENCH calibrate:" !in line) return null
        val spin = field(line, "spinNs")
        val gap = field(line, "gapNs")
        return Calibration(
            label = field(line, "label") ?: return null,
            spun = field(line, "spun")?.toIntOrNull(),
            spin = nanosecondsToMilliseconds(spin),
            spinRaw = spin,
            window = nanosecondsToMilliseconds(field(line, "windowNs")),
            gap = nanosecondsToMilliseconds(gap),
            gapRaw = gap
        )
    }

    private fun parse(line: String): Measurement? {
        val kind = when {
            "SKRIPTORM_BENCH=COMPARE" in line ->
                Regex("SKRIPTORM_BENCH=COMPARE (pluginwrite|pluginread|rawwrite|rawread):")
                    .find(line)?.groupValues?.get(1) ?: return null
            "SKRIPTORM_BENCH write:" in line -> "write"
            "SKRIPTORM_BENCH read:" in line -> "read"
            "SKRIPTORM_BENCH warmwrite:" in line -> "warmwrite"
            "SKRIPTORM_BENCH warmread:" in line -> "warmread"
            else -> return null
        }
        val rows = field(line, "rows")?.toIntOrNull() ?: return null
        val wall = field(line, "wallNs")
        val gap = field(line, "gapNs")
        return Measurement(
            kind = kind,
            rows = rows,
            ticks = field(line, "ticks")?.toIntOrNull() ?: -1,
            wall = nanosecondsToMilliseconds(wall),
            wallRaw = wall,
            gap = nanosecondsToMilliseconds(gap),
            gapRaw = gap,
            affected = field(line, "affected")?.toIntOrNull(),
            stored = field(line, "stored")?.toIntOrNull(),
            total = field(line, "total")?.toIntOrNull(),
            error = Regex("""error=(.*)$""").find(line)?.groupValues?.get(1)?.trim()
                ?.takeIf { it.isNotEmpty() && it != "<none>" }
        )
    }

    private fun field(line: String, name: String): String? =
        Regex("""\b${Regex.escape(name)}=(\[[^]]*]|\S+)""").find(line)?.groupValues?.get(1)

    /** Shows a nanosecond reading as milliseconds, or marks it unreadable. */
    private fun milliseconds(value: Double?, raw: String?): String =
        value?.let { "%.6f ms".format(it) } ?: describe(raw)

    /** Describes a value that could not be parsed. */
    private fun describe(raw: String?): String = when (raw?.trim()) {
        null -> "-"
        "<none>" -> "none"
        else -> "unparsed"
    }

    /** Converts a logged nanoTime difference to milliseconds for the console report. */
    private fun nanosecondsToMilliseconds(raw: String?): Double? =
        nanoseconds(raw)?.div(1_000_000.0)

    private fun nanoseconds(raw: String?): Long? =
        raw?.trim()?.removeSurrounding("[", "]")?.replace(",", "")?.toLongOrNull()

    /** A calibration window and the longest tick-start gap observed within it. */
    private data class Calibration(
        val label: String,
        val spun: Int?,
        val spin: Double?,
        val spinRaw: String?,
        val window: Double?,
        val gap: Double?,
        val gapRaw: String?
    )

    private data class Measurement(
        val kind: String,
        val rows: Int,
        val ticks: Int,
        val wall: Double?,
        val wallRaw: String?,
        val gap: Double?,
        val gapRaw: String?,
        val affected: Int?,
        val stored: Int?,
        val total: Int?,
        val error: String?
    )

    private companion object {
        /** A tick's own length, so an overrun can be told from a tick that did its normal work. */
        const val TICK_MILLIS = 50.0
    }
}
