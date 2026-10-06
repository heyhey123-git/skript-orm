package buildlogic.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption

abstract class DownloadFile : DefaultTask() {

    @get:Input
    abstract val url: Property<String>

    @get:OutputFile
    abstract val target: RegularFileProperty

    @TaskAction
    fun download() {
        val file = target.get().asFile
        // The version is part of the name, so a file that is already here is the file this url names:
        // asking the network again for something it has already delivered is wasted, and on a machine
        // where the download is slow or blocked it is the difference between working and not.
        if (file.isFile && file.length() > 0L) {
            logger.lifecycle("{} is already downloaded.", file.name)
            return
        }
        file.parentFile.mkdirs()
        // Downloaded beside the real name and then moved onto it, so an interrupted transfer is never
        // taken for the complete file by the next run.
        val partial = file.resolveSibling(file.name + ".part")
        var last: IOException? = null
        for (attempt in 1..ATTEMPTS) {
            try {
                URI(url.get()).toURL().openStream().use { input ->
                    partial.outputStream().use { output -> input.copyTo(output) }
                }
                // Replacing is what makes a second download work: the file it moves onto is usually already
                // there, and a plain rename refuses to overwrite on Windows.
                Files.move(partial.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                if (attempt > 1) logger.lifecycle("{} arrived on attempt {}.", file.name, attempt)
                return
            } catch (missing: FileNotFoundException) {
                // The url names nothing, and will name nothing on the next attempt either.
                throw GradleException("Could not download ${url.get()}: ${missing.message}", missing)
            } catch (error: IOException) {
                // An asset host under load answers 502, 503 or 504 to a runner, and delivers the same file
                // to the next request: the release this workflow publishes is worth another try.
                last = error
                partial.delete()
                if (attempt < ATTEMPTS) {
                    logger.lifecycle("{} did not arrive ({}), trying again.", file.name, error.message)
                    Thread.sleep(PAUSE_MILLIS * attempt)
                }
            }
        }
        throw GradleException("Could not download ${url.get()}: ${last?.message}", last)
    }

    private companion object {
        /** Attempts at one download, and the pause before each retry, growing with the attempt number. */
        const val ATTEMPTS = 4
        const val PAUSE_MILLIS = 2_000L
    }
}
