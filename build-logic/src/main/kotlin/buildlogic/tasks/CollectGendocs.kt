package buildlogic.tasks

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class CollectGendocs : DefaultTask() {

    /** What the tool wrote on the server that just ran. */
    @get:InputFile
    abstract val generated: RegularFileProperty

    /** Where the generated file is left, for a person to paste into SkriptHub. */
    @get:OutputFile
    abstract val destination: RegularFileProperty

    /** The version the documentation has to report, which is the one in `gradle.properties`. */
    @get:Input
    abstract val expectedVersion: Property<String>

    @TaskAction
    fun collect() {
        val source = generated.get().asFile
        if (!source.isFile) {
            throw GradleException(
                "The documentation tool wrote no ${source.name}, so either the server never ran it or " +
                    "it failed to; its log is beside the run directory, at logs/latest.log."
            )
        }

        @Suppress("UNCHECKED_CAST")
        val document = JsonSlurper().parse(source) as Map<String, Any?>
        // Every kind Skript documents, so that one this addon does not use is noticed rather than
        // quietly skipped. It has none of the kinds named before effects.
        val kinds = listOf(
            "events",
            "conditions",
            "effects",
            "expressions",
            "types",
            "functions",
            "sections",
            "structures"
        )
        val entries = kinds.flatMap { kind ->
            val ofKind = document[kind] as? List<*> ?: emptyList<Any?>()
            ofKind.filterIsInstance<Map<*, *>>().map { kind to it }
        }

        val problems = mutableListOf<String>()

        val metadata = document["metadata"] as? Map<*, *>
        val reportedVersion = metadata?.get("version")?.toString().orEmpty()
        if (reportedVersion != expectedVersion.get()) {
            problems += "The documentation reports version '$reportedVersion' where this build is " +
                "${expectedVersion.get()}."
        }
        if (entries.isEmpty()) {
            problems += "The file lists no syntax at all."
        }

        entries.forEach { (kind, entry) ->
            val name = entry["name"]?.toString().orEmpty()
            val label = if (name.isBlank()) "a ${kind.dropLast(1)}" else "'$name'"
            if (name.isBlank()) problems += "A ${kind.dropLast(1)} has no name."

            val patterns = (entry["patterns"] as? List<*>)?.map { it.toString() }.orEmpty()
            if (patterns.none { it.isNotBlank() }) problems += "$label lists no pattern."

            val description = (entry["description"] as? List<*>)?.map { it.toString() }.orEmpty()
            if (description.none { it.isNotBlank() }) problems += "$label has no description."

            val examples = (entry["examples"] as? List<*>)?.map { it.toString() }.orEmpty()
            if (examples.isEmpty()) {
                problems += "$label has no example."
            } else if (examples.any { it.startsWith("\n") || it.startsWith("\r") }) {
                problems += "$label has an example that starts on a blank line, which SkriptHub shows as " +
                    "an empty first line: start the raw string on the same line as the first line of Skript."
            }
        }

        val duplicates = entries.groupBy { it.second["id"]?.toString() }.filterValues { it.size > 1 }.keys
        if (duplicates.isNotEmpty()) {
            problems += "Two entries share an id, which SkriptHub refuses: ${duplicates.joinToString()}."
        }

        if (problems.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("The generated documentation cannot be uploaded as it is:")
                    problems.forEach { appendLine("  - $it") }
                    appendLine("What the tool wrote is left at ${source.absolutePath}.")
                }
            )
        }

        val target = destination.get().asFile
        target.parentFile.mkdirs()
        target.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(withoutBlankEdges(document))))
        logger.lifecycle(
            "{} entries written to {}, ready for SkriptHub's JSON import.",
            entries.size,
            target.name
        )
    }

    /**
     * The document with the blank lines around every example taken off.
     *
     * An `@Example` written as a raw string carries the newline after its opening quotes and the one
     * before its closing quotes, because those closing quotes sit on a line of their own — which is how
     * every example here is written, and what keeps the Skript lines at column zero instead of indented
     * along with the annotation. Neither newline means anything to SkriptHub, which shows each as an
     * empty first or last line of the example, so both ends are taken off here. The opening one is no
     * longer written by the annotations, and the check above keeps it that way.
     */
    private fun withoutBlankEdges(document: Map<String, Any?>): Map<String, Any?> {
        val trimmedDocument = LinkedHashMap<String, Any?>(document.size)
        document.forEach { (key, value) ->
            val entries = value as? List<*>
            if (entries == null) {
                trimmedDocument[key] = value
                return@forEach
            }
            trimmedDocument[key] = entries.map { entry ->
                val fields = entry as? Map<*, *> ?: return@map entry
                val trimmedFields = LinkedHashMap<Any?, Any?>(fields.size)
                fields.forEach { (field, fieldValue) ->
                    val examples = if (field == "examples") fieldValue as? List<*> else null
                    trimmedFields[field] = examples
                        ?.map { example -> trimBlankEdges(example.toString()) }
                        ?: fieldValue
                }
                trimmedFields
            }
        }
        return trimmedDocument
    }

    /** [example] without blank lines at either end, keeping the indentation of the lines it has. */
    private fun trimBlankEdges(example: String): String {
        val lines = example.split("\n").toMutableList()
        while (lines.size > 1 && lines.first().isBlank()) lines.removeAt(0)
        while (lines.size > 1 && lines.last().isBlank()) lines.removeAt(lines.size - 1)
        return lines.joinToString("\n")
    }
}
