package buildlogic.plugins

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering

class ReleaseNotesPlugin : Plugin<Project> {
    override fun apply(projectTarget: Project) {
        with(projectTarget) {
            val releaseNotesFile = layout.buildDirectory.file("release-notes.md")

            val releaseNotes by tasks.registering {
                description = "Writes the CHANGELOG.md section of the current version to build/release-notes.md."
                group = "documentation"

                val changelog = layout.projectDirectory.file("CHANGELOG.md")
                val notes = releaseNotesFile
                // The version being released, unless an older one is named: a release that is already published can be
                // corrected with the same task, which is what `-PreleaseNotesVersion=1.0.0` is for.
                val releasedVersion = providers.gradleProperty("releaseNotesVersion").orNull ?: version.toString()

                inputs.file(changelog).withPropertyName("changelog")
                inputs.property("version", releasedVersion)
                outputs.file(notes).withPropertyName("releaseNotes")

                doLast {
                    // Matched by heading rather than by position, because the file is written newest first and a
                    // version that is not there at all has to be an error rather than an empty release.
                    val heading = Regex("""(?m)^## \[${Regex.escape(releasedVersion)}\][^\n]*$""")
                    val text = changelog.asFile.readText()
                    val section = heading.find(text) ?: throw GradleException(
                        "CHANGELOG.md has no '## [$releasedVersion]' section. Write what this release changes before " +
                            "releasing it, so the release describes itself."
                    )
                    val rest = text.substring(section.range.last + 1)
                    val nextHeading = Regex("""(?m)^## \[[^\n]*$""").find(rest)
                    val body = (if (nextHeading == null) rest else rest.substring(0, nextHeading.range.first)).trim()
                    if (body.isEmpty()) {
                        throw GradleException("The '## [$releasedVersion]' section of CHANGELOG.md has no body.")
                    }
                    notes.get().asFile.apply { parentFile.mkdirs() }.writeText("$body\n")
                    logger.lifecycle("The release notes for {} are in {}.", releasedVersion, notes.get().asFile)
                }
            }
        }
    }
}
