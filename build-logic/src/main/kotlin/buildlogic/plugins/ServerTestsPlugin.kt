package buildlogic.plugins

import buildlogic.ServerTestImplementation
import buildlogic.tasks.VerifySkriptServerTest
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import xyz.jpenilla.runpaper.task.RunServer

class ServerTestsPlugin : Plugin<Project> {
    override fun apply(projectTarget: Project) {
        with(projectTarget) {
            pluginManager.apply("skript-orm.server-runtime")
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            val runtime = extensions.getByType<buildlogic.ServerRuntime>()
            val paperMinecraftVersion = runtime.minecraftVersion
            val paperBuild = runtime.paperBuild
            val serverTestPlugins = runtime.plugins
            val bundledModules = buildlogic.selectedModules(project)

            val serverTestDirectory = layout.buildDirectory.dir("server-test")
            val serverTestSource = layout.projectDirectory.dir("server-test")

            val serverTestImplementations = mapOf(
                "MySQL" to ServerTestImplementation(keys = "mysql", module = null, username = "root"),
                "PostgreSQL" to ServerTestImplementation(
                    keys = "postgres",
                    module = "postgresql-implementation",
                    username = "postgres"
                ),
                // No username, because a MongoDB that CI starts has none to give. The type name is the name a script
                // writes and the same word as the product, so its keys are `skriptorm.test.mongo.*`.
                "MongoDB" to ServerTestImplementation(
                    keys = "mongo",
                    module = "mongodb-implementation",
                    username = "",
                    transactions = false
                )
            )

            val serverTestDatabaseType = providers.gradleProperty("skriptorm.test.server.type").orElse("MySQL")
            val serverTestImplementation: ServerTestImplementation = serverTestImplementations[serverTestDatabaseType.get()]
                ?: error(
                    "skriptorm.test.server.type must name one of ${serverTestImplementations.keys}, " +
                        "but was '${serverTestDatabaseType.get()}'."
                )

            // No fallback for the url: whether one was given is what decides between the two modes, so an empty one
            // would turn every run into a run that tries to connect. An empty password is a real one, though.
            fun serverTestSetting(name: String): Provider<String> =
                providers.gradleProperty("skriptorm.test.${serverTestImplementation.keys}.$name")
                    .orElse(
                        providers.environmentVariable(
                            "SKRIPTORM_TEST_${serverTestImplementation.keys.uppercase()}_${name.uppercase()}"
                        )
                    )

            val serverTestDatabaseUrl = serverTestSetting("url")
            val serverTestDatabaseUsername = serverTestSetting("username").orElse(serverTestImplementation.username)
            val serverTestDatabasePassword = serverTestSetting("password").orElse("")
            val serverTestUsesDatabase = serverTestDatabaseUrl.isPresent

            val prepareServerTest by tasks.registering {
                description = "Writes the run directory the Skript server test starts from."
                group = "verification"

                val runDirectory = serverTestDirectory
                val sourceDirectory = serverTestSource
                // Read before the task runs: inside `doLast` the script's own project is out of reach.
                val examplesDirectory = layout.projectDirectory.dir("docs/examples")

                doLast {
                    // A type name reaches the server only if the jar it loads registers it, so a run against an
                    // implementation whose module is not bundled would start a whole server to be told
                    // `Database 'PostgreSQL' is not supported.` — it says what to add before that.
                    serverTestImplementation.module?.let { module ->
                        if (serverTestUsesDatabase && module !in bundledModules) {
                            throw GradleException(
                                "The server test cannot connect to ${serverTestDatabaseType.get()}: the jar this run " +
                                    "installs does not carry that implementation. Invoke it with " +
                                    "-PbundleModules=${(bundledModules + module).distinct().joinToString(",")}."
                            )
                        }
                    }

                    val run = runDirectory.get().asFile
                    // Skript's whole plugin directory goes, not just its scripts: every element script keeps a
                    // guard variable so that it runs once, and Skript stores variables there. A guard left over
                    // from an earlier run would make the next one skip that element and look like a failure.
                    // Skript writes its config back when it next starts.
                    run.resolve("plugins/Skript").deleteRecursively()
                    val scripts = run.resolve("plugins/Skript/scripts")
                    scripts.mkdirs()
                    // Copied as a tree, because the elements live one file each under `elements/`.
                    sourceDirectory.dir("skript").asFile.copyRecursively(scripts, overwrite = true)
                    // The examples the documentation shows come along so that Skript parses them: a page that
                    // teaches syntax the plugin does not have then fails this test rather than reaching a reader.
                    // They are never run, and their check is the same "can't understand" one below.
                    examplesDirectory.asFile.copyRecursively(
                        scripts.resolve("examples"),
                        overwrite = true
                    )
                    // The database mode adds a setup script and a round trip on top of the same element scripts,
                    // and may replace one: it is copied over the tree above, so a file of the same name and path
                    // wins. Only `elements/16-disconnect.sk` does, because it is the one element that would close
                    // the connection the round trip is still using. The setup is generated because its
                    // credentials come from the properties, not the repository.
                    if (serverTestUsesDatabase) {
                        sourceDirectory.dir("database").asFile.copyRecursively(scripts, overwrite = true)
                        // Every script carrying the placeholders is filled in, not only the setup: the connection
                        // element opens a second connection of its own and needs the same implementation and the
                        // same credentials.
                        val type = serverTestDatabaseType.get()
                        val url = serverTestDatabaseUrl.get()
                        val username = serverTestDatabaseUsername.get()
                        val password = serverTestDatabasePassword.get()
                        scripts.walkTopDown()
                            .filter { it.extension == "sk" }
                            .forEach { script ->
                                val text = script.readText()
                                if ("__URL__" !in text) return@forEach
                                script.writeText(
                                    text.replace("__TYPE__", type)
                                        .replace("__URL__", url)
                                        .replace("__USERNAME__", username)
                                        .replace("__PASSWORD__", password)
                                )
                            }
                    }
                    // The finisher is written rather than copied, because its count is the number of scripts that
                    // report a detail line. Deriving it means adding an element cannot leave a stale number behind.
                    val finisher = scripts.resolve("99-finish.sk")
                    val reporting = scripts.walkTopDown()
                        .filter { it.extension == "sk" && it != finisher }
                        .count { "SKRIPTORM_SELFTEST detail:" in it.readText() }
                    finisher.writeText(finisher.readText().replace("__ELEMENT_COUNT__", reporting.toString()))
                    sourceDirectory.file("server.properties").asFile
                        .copyTo(run.resolve("server.properties"), overwrite = true)
                    // Paper refuses to start without this. Writing it records acceptance of the Minecraft EULA
                    // (https://aka.ms/MinecraftEULA) for this disposable test server, which is the only server
                    // this build ever writes the file for.
                    run.resolve("eula.txt").writeText("eula=true\n")
                }
            }

            tasks.named<RunServer>("runServer") {
                dependsOn(prepareServerTest)
                minecraftVersion(paperMinecraftVersion)
                build(paperBuild)
                runDirectory.set(serverTestDirectory)
                // The shaded jar is the plugin under test. run-paper would find it on its own; naming it here
                // keeps the choice of artifact visible.
                pluginJars(tasks.named("shadowJar"), serverTestPlugins)
                // SkBee is not a dependency of this plugin. The server test installs it so the NBT interop, which
                // only exists on a server that has SkBee, is exercised against the real thing. Pinned by
                // Modrinth's version id, which is not the plugin version: `bTBlzhGZ` is SkBee 3.25.4.
                downloadPlugins {
                    modrinth("skbee", "bTBlzhGZ")
                }
            }

            // One entry per script the self-test drives: the name it reports, and the message it reports with.
            // Without a database every section stops at the database lookup and says so through
            // `last database error`; with one they succeed, and how far each got depends on the order Skript
            // fires them in, so an empty expectation means "it reported, whatever it said" and the round trip
            // carries the assertions that matter. Keeping the list here rather than in the scripts means a script
            // added to one without the other fails the test instead of passing unnoticed.
            val expectations = buildlogic.ServerTestExpectations(serverTestUsesDatabase, serverTestImplementation)
            val serverTestChecks = expectations.serverTestChecks
            val serverTestDatabaseChecks = expectations.serverTestDatabaseChecks

            // Every name the scripts under `server-test` report, taken from the sources rather than from what this
            // run copied: the database scripts are only copied when a database is configured, and a name they
            // report without an entry in the checks would otherwise be found by the one job that runs with a
            // database, a CI round trip away. Only the names no mode lists are handed to the check below.
            val serverTestDetailLine = Regex("""SKRIPTORM_SELFTEST detail:\s*(.+?)\s*->""")
            val serverTestUndeclaredChecks = providers.provider {
                val declared = serverTestSource.asFile.walkTopDown()
                    .filter { it.isFile && it.extension == "sk" }
                    .flatMap { file ->
                        serverTestDetailLine.findAll(file.readText()).map { it.groupValues[1] }.asSequence()
                    }
                    .toSet()
                declared - (serverTestChecks.keys + serverTestDatabaseChecks.keys)
            }

            val serverTest by tasks.registering(VerifySkriptServerTest::class) {
                description = "Boots a Paper server with Skript and checks what this plugin did there."
                group = "verification"
                dependsOn(tasks.named("runServer"))
                serverLog.set(serverTestDirectory.map { directory -> directory.file("logs/latest.log") })
                expectedPluginVersion.set(version.toString())
                expectedSkriptVersion.set(libs.findVersion("skript").get().requiredVersion)
                expectedChecks.set(serverTestChecks)
                undeclaredChecks.set(serverTestUndeclaredChecks)
            }
        }
    }
}
