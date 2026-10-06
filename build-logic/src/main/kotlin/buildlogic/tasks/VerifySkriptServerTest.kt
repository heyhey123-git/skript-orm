package buildlogic.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class VerifySkriptServerTest : DefaultTask() {

    /** The log of the server that just ran. */
    @get:Internal
    abstract val serverLog: RegularFileProperty

    /** The version Paper has to report for the plugin it loaded. */
    @get:Input
    abstract val expectedPluginVersion: Property<String>

    /** The version the Skript under test has to report. */
    @get:Input
    abstract val expectedSkriptVersion: Property<String>

    /** The elements the self-test has to report, mapped to the message it has to report with them. */
    @get:Input
    abstract val expectedChecks: MapProperty<String, String>

    /**
     * Names the scripts under `server-test` report that neither mode lists. Computed from the sources,
     * so a name added to a script is reported in every mode rather than by the one job that copies that
     * script.
     */
    @get:Input
    abstract val undeclaredChecks: SetProperty<String>

    @TaskAction
    fun checkLog() {
        val log = serverLog.get().asFile
        if (!log.isFile) {
            throw GradleException("The server wrote no log at ${log.absolutePath}.")
        }
        // A failing run leaves its problems beside the log, and a later passing one would otherwise leave
        // that file sitting there saying the opposite of what happened.
        log.parentFile.resolve("test-problems.txt").delete()

        val lines = log.readLines()
        val problems = mutableListOf<String>()

        fun requireInLog(problem: String, text: String) {
            if (lines.none { text in it }) problems += "$problem\n      absent: $text"
        }

        requireInLog(
            "Paper never enabled this plugin, so the addon was not under test at all.",
            "Enabling skript-orm v${expectedPluginVersion.get()}"
        )
        requireInLog(
            "The server did not run the Skript this plugin is built against.",
            "Enabling Skript v${expectedSkriptVersion.get()}"
        )
        requireInLog(
            "The self-test never reached its end, so a section it drives did not get through.",
            "SKRIPTORM_SELFTEST=PASS"
        )
        // A write past the row ceiling is sent as several statements and every row is written, which the
        // script asserts by counting the table itself. Nothing is dropped there, so there is no warning left
        // for this side to look for: the count assertion is the whole of it, and a plugin that cut the batch
        // short reports a FAIL line the check below turns into a problem.

        val failures = lines.filter { "SKRIPTORM_SELFTEST=FAIL" in it }
        if (failures.isNotEmpty()) {
            // One line each, because these lines are what CI annotates and annotations are capped.
            problems += failures.map { "The self-test reported: ${it.substringAfter("SKRIPTORM_SELFTEST=").trim()}" }
        }

        // "SKRIPTORM_SELFTEST detail: <element> -> <message>"
        val reported = lines
            .mapNotNull { line -> line.substringAfter("SKRIPTORM_SELFTEST detail: ", "").ifEmpty { null } }
            .associate { line -> line.substringBeforeLast(" -> ") to line.substringAfterLast(" -> ") }

        expectedChecks.get().forEach { (element, message) ->
            val actual = reported[element]
            when {
                actual == null -> problems += "The self-test did not reach '$element'."
                // An empty expectation means the line only has to be there. The database mode cannot
                // predict how far each element got before the round trip states the real assertions.
                message.isEmpty() -> Unit
                // An expectation is checked as a prefix, so an entry that has to account for the whole line
                // writes the whole line. The rule exists for the lines whose log appends a clause about
                // connections the reporting script does not own, and no other line here starts with another's
                // expectation.
                actual == message || actual.startsWith(message) -> Unit
                else -> problems += "'$element' reported '$actual' instead of '$message'."
            }
        }

        (reported.keys - expectedChecks.get().keys).forEach { unexpected ->
            problems += "The self-test reported '$unexpected', which this check does not know about."
        }

        // The same mismatch, found before a run rather than in it: a script reports a name no mode
        // lists, which only a mode that copies that script would otherwise have said.
        undeclaredChecks.get().sorted().forEach { undeclared ->
            problems += "A script under server-test reports '$undeclared', which no check lists."
        }

        // A statement Skript cannot match against any registered pattern is dropped, and the run
        // carries on looking healthy around it, so this is checked on its own.
        val unparsed = lines.filter { line -> line.contains("can't understand", ignoreCase = true) }
        if (unparsed.isNotEmpty()) {
            problems += "Skript could not parse part of a test script:\n" + indent(unparsed) +
                "\n      A line from `docs/examples` here means a page shows syntax the plugin does " +
                "not have."
        }

        // A pattern Skript refuses to register, or one it cannot build an expression for, is reported as
        // a severe error and the line that used it is dropped: the file then loads with a piece missing
        // and every check above still passes. The first transaction pattern was written as
        // `[on connection %string%]`, which Skript rejects, and only this line would have said so.
        val severe = lines.filter { line -> line.contains("[Skript] Severe Error") }
        if (severe.isNotEmpty()) {
            problems += "Skript reported a severe error while loading a test script:\n" + indent(severe)
        }

        if (problems.isNotEmpty()) {
            // A failing step's log needs admin rights to read, while annotations are public, so the
            // problems are also written next to the log for CI to annotate. Without this, a failure
            // here is only visible to whoever can open the job.
            //
            // The database elements report whatever their step produced rather than a fixed message,
            // so the round trip carries the assertions and those probe lines are the only evidence of
            // what the write and the reads actually did. CI keeps only the last few annotations, so
            // the probes are packed into one line to leave room for the failures above them.
            val probes = listOf("setup", "setup select", "roundtrip", "roundtrip now", "roundtrip later", "roundtrip by id")
                .mapNotNull { element -> reported[element]?.let { "$element -> $it" } }
            val evidence = buildList {
                add("The Skript server test failed:")
                addAll(problems)
                if (probes.isNotEmpty()) add("What the database probes printed: " + probes.joinToString(" | "))
            }
            serverLog.get().asFile.parentFile.resolve("test-problems.txt").writeText(evidence.joinToString("\n"))
            throw GradleException(
                buildString {
                    appendLine("The Skript server test failed:")
                    problems.forEach { appendLine("  - $it") }
                    appendLine()
                    appendLine("What the test reported (last 40 matching log lines):")
                    appendLine(indent(lines.filter { "SKRIPTORM_SELFTEST" in it }.takeLast(40)))
                    appendLine()
                    appendLine("What Skript said about the test scripts:")
                    appendLine(indent(reportedScriptProblems(lines).takeLast(40)))
                }
            )
        }

        logger.lifecycle(
            "The Skript server test passed: {} elements ran, and Skript {} parsed them all.",
            expectedChecks.get().size,
            expectedSkriptVersion.get()
        )
    }

    /**
     * Everything Skript logged about the test scripts, whatever level it used.
     *
     * Skript reports a script it could not read through these lines, and it notes a section without a
     * body this way too, which the addon's statements do use. They explain a failure rather than
     * cause one, so they are reported with it instead of checked against it.
     */
    private fun reportedScriptProblems(lines: List<String>): List<String> =
        lines.filter { line -> Regex("""\[Skript] Line \d+: \(""").containsMatchIn(line) }

    private fun indent(lines: List<String>): String =
        lines.joinToString(separator = "\n") { line -> "      $line" }
}
