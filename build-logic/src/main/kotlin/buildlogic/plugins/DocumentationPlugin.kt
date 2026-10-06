package buildlogic.plugins

import buildlogic.tasks.CollectGendocs
import buildlogic.tasks.DownloadFile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import xyz.jpenilla.runpaper.task.RunServer

class DocumentationPlugin : Plugin<Project> {
    override fun apply(projectTarget: Project) {
        with(projectTarget) {
            pluginManager.apply("skript-orm.server-runtime")
            val runtime = extensions.getByType<buildlogic.ServerRuntime>()
            val paperMinecraftVersion = runtime.minecraftVersion
            val paperBuild = runtime.paperBuild
            val serverTestPlugins = runtime.plugins

            val skriptHubDocsToolVersion = "1.17"

            // Kept in the Gradle user home rather than under build/, because the release workflow restores that
            // directory between runs and this jar is the one part of the documentation run that comes off the network:
            // a machine that has it once should never have to ask for it again, and the asking is what fails when
            // GitHub's asset host answers a runner with 504.
            val skriptHubDocsToolFile = gradle.gradleUserHomeDir.resolve(
                "caches/skript-hub-docs/skripthubdocstool-$skriptHubDocsToolVersion.jar"
            )
            val skriptHubDocsDirectory = layout.buildDirectory.dir("skript-hub")

            val downloadSkriptHubDocsTool by tasks.registering(DownloadFile::class) {
                description = "Downloads the tool that generates the SkriptHub documentation."
                group = "documentation"
                url.set(
                    "https://github.com/SkriptHub/SkriptHubDocsTool/releases/download/" +
                        "$skriptHubDocsToolVersion/skripthubdocstool-$skriptHubDocsToolVersion.jar"
                )
                target.set(skriptHubDocsToolFile)
            }

            // The documentation server gets a run directory of its own rather than sharing the test one: the test's
            // scripts stop the server as soon as they have all reported, which is the opposite of what this run
            // wants. Its first run also downloads and extracts Paper there.
            val prepareGendocs by tasks.registering {
                description = "Writes the run directory the documentation server starts from."
                group = "documentation"
                val runDirectory = skriptHubDocsDirectory
                doLast {
                    val run = runDirectory.get().asFile
                    run.mkdirs()
                    // The run directory outlives a run, and any script left in it is parsed and executed next time:
                    // this is where a check script once ran a connection example while the documentation was being
                    // generated. Skript writes its config back when it next starts, so the whole directory goes.
                    run.resolve("plugins/Skript").deleteRecursively()
                    // As in the server test: this records acceptance of the Minecraft EULA, and only ever for a
                    // disposable server this build creates.
                    run.resolve("eula.txt").writeText("eula=true\n")
                    // A server that never accepts a player. The port differs from the test server's so both can be
                    // up at once, and the pause is off because an empty server would otherwise stop ticking, and
                    // the script below would then never run.
                    run.resolve("server.properties").writeText(
                        """
                    online-mode=false
                    server-port=25600
                    level-type=minecraft:flat
                    generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}
                    level-name=world
                    spawn-protection=0
                    max-players=1
                    view-distance=2
                    simulation-distance=2
                    difficulty=peaceful
                    enable-command-block=false
                    enable-status=false
                    pause-when-empty-seconds=-1
                    sync-chunk-writes=false
                        """.trimIndent() + "\n"
                    )
                    // The tool generates from a command, and a server started by Gradle has no console to type into,
                    // so a script runs it and then stops the server this task is waiting on.
                    val script = run.resolve("plugins/Skript/scripts/gendocs.sk")
                    script.parentFile.mkdirs()
                    script.writeText(
                        """
                    # Written by the prepareGendocs task. Not part of the server test.
                    on load:
                        wait 1 second
                        execute console command "/gendocs"
                        wait 2 seconds
                        execute console command "/stop"
                        """.trimIndent() + "\n"
                    )
                }
            }

            val runGendocs by tasks.registering(RunServer::class) {
                description = "Boots the server the documentation is generated on."
                group = "documentation"
                dependsOn(prepareGendocs, downloadSkriptHubDocsTool)
                minecraftVersion(paperMinecraftVersion)
                build(paperBuild)
                runDirectory.set(skriptHubDocsDirectory)
                pluginJars(tasks.named("shadowJar"), serverTestPlugins, files(skriptHubDocsToolFile))
                // SkBee comes along because the NBT column type is the one whose implementation lives in another
                // plugin, and the documentation is generated from what the server actually has.
                downloadPlugins {
                    modrinth("skbee", "bTBlzhGZ")
                }
            }

            val gendocs by tasks.registering(CollectGendocs::class) {
                description = "Generates the SkriptHub documentation JSON from the annotations in the code."
                group = "documentation"
                dependsOn(runGendocs)
                // The file is named after the plugin, which is the name SkriptHub knows the addon by.
                generated.set(
                    skriptHubDocsDirectory.map { directory ->
                        directory.file("plugins/SkriptHubDocsTool/documentation/skript-orm.json")
                    }
                )
                destination.set(layout.buildDirectory.file("skripthub/skript-orm.json"))
                expectedVersion.set(version.toString())
            }
        }
    }
}
