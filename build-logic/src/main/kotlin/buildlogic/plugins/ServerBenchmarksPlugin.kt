package buildlogic.plugins

import buildlogic.tasks.DownloadFile
import buildlogic.tasks.ReportSkriptServerBenchmark
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import xyz.jpenilla.runpaper.task.RunServer
import java.util.zip.ZipFile

class ServerBenchmarksPlugin : Plugin<Project> {
    override fun apply(projectTarget: Project) {
        with(projectTarget) {
            pluginManager.apply("skript-orm.server-runtime")
            val runtime = extensions.getByType<buildlogic.ServerRuntime>()
            val paperMinecraftVersion = runtime.minecraftVersion
            val paperBuild = runtime.paperBuild
            val serverTestPlugins = runtime.plugins

            val serverBenchmarkDirectory = layout.buildDirectory.dir("server-benchmark")
            val serverBenchmarkSource = layout.projectDirectory.dir("server-benchmark")
            // Where the medians are written as JSON, so the CI job can record them per CPU into the gh-pages history
            // beside the JMH numbers. The root task writes to the root build directory; the JMH module's report lives
            // in `benchmarks/build/benchmarks/` because it belongs to that module.
            val serverBenchmarkResults = layout.buildDirectory.file("benchmarks/tick-results.json")
            val skriptReflectVersion = "2.6.3"
            val skriptReflectJar = layout.buildDirectory.file("server-benchmark/plugins/skript-reflect-$skriptReflectVersion.jar")

            val benchmarkDatabaseType = providers.gradleProperty("skriptorm.benchmark.server.type").orElse("JDBC")
            val benchmarkTypes = setOf("JDBC", "MySQL", "MariaDB", "PostgreSQL", "MongoDB")
            require(benchmarkDatabaseType.get() in benchmarkTypes) {
                "skriptorm.benchmark.server.type must be one of $benchmarkTypes."
            }
            val benchmarkDatabaseUrl = providers.gradleProperty("skriptorm.benchmark.url").orElse(
                if (benchmarkDatabaseType.get() == "JDBC") {
                    "jdbc:sqlite:plugins/Skript/bench-probe.db?synchronous=OFF&journal_mode=MEMORY"
                } else {
                    ""
                }
            )
            val benchmarkDatabaseUsername = providers.gradleProperty("skriptorm.benchmark.username").orElse("")
            val benchmarkDatabasePassword = providers.gradleProperty("skriptorm.benchmark.password").orElse("")

            val prepareServerBenchmark by tasks.registering {
                description = "Writes the run directory the Skript benchmark starts from."
                group = "verification"

                val runDirectory = serverBenchmarkDirectory
                val sourceDirectory = serverBenchmarkSource
                // The same properties the self-test uses: same Paper, same flat world, same short boot.
                val propertiesFile = layout.projectDirectory.file("server-test/server.properties")

                doLast {
                    val run = runDirectory.get().asFile
                    // Skript keeps its variables in its plugin directory, and a guard variable left over from an
                    // earlier run would make the next one skip the work it was asked to do.
                    run.resolve("plugins/Skript").deleteRecursively()
                    val scripts = run.resolve("plugins/Skript/scripts")
                    scripts.mkdirs()
                    sourceDirectory.dir("skript").asFile.copyRecursively(scripts, overwrite = true)
                    // These lists are temporary benchmark inputs, not persistent server data. Exclude them
                    // from Skript's save queue so repeated global-list cases cannot create a CSV backlog.
                    val skriptJar = serverTestPlugins.singleFile
                    ZipFile(skriptJar).use { archive ->
                        val entry = requireNotNull(archive.getEntry("config.sk")) { "Skript jar contains no default config." }
                        val config = archive.getInputStream(entry).bufferedReader().use { it.readText() }
                            .replace("pattern: .*", "pattern: (?!bench::).*")
                        run.resolve("plugins/Skript/config.sk").writeText(config)
                    }
                    require(benchmarkDatabaseUrl.get().isNotBlank()) { "Set skriptorm.benchmark.url for this backend." }
                    val settings = mapOf(
                        "__BENCH_TYPE__" to benchmarkDatabaseType.get(),
                        "__BENCH_URL__" to benchmarkDatabaseUrl.get(),
                        "__BENCH_USERNAME__" to benchmarkDatabaseUsername.get(),
                        "__BENCH_PASSWORD__" to benchmarkDatabasePassword.get()
                    )
                    scripts.walkTopDown().filter { it.extension == "sk" }.forEach { script ->
                        var text = script.readText()
                        for ((placeholder, value) in settings) {
                            require('\n' !in value && '\r' !in value) { "Benchmark settings must fit on one line." }
                            text = text.replace(placeholder, value.replace("\"", "\"\"").replace("%", "%%"))
                        }
                        script.writeText(text)
                    }
                    propertiesFile.asFile.copyTo(run.resolve("server.properties"), overwrite = true)
                    // Paper refuses to start without this. Writing it records acceptance of the Minecraft EULA
                    // (https://aka.ms/MinecraftEULA) for this disposable benchmark server.
                    run.resolve("eula.txt").writeText("eula=true\n")
                }
            }

            val runServerBenchmark by tasks.registering(RunServer::class) {
                description = "Boots the Paper server the Skript benchmark runs on."
                group = "verification"

                dependsOn(prepareServerBenchmark)
                dependsOn("downloadSkriptReflectForBenchmark")
                minecraftVersion(paperMinecraftVersion)
                build(paperBuild)
                runDirectory.set(serverBenchmarkDirectory)
                jvmArgs("-Dskriptorm.benchmark.timings=true")
                // skript-reflect is downloaded into plugins/ and discovered there by Paper.
                pluginJars(tasks.named("shadowJar"), serverTestPlugins)
                // These cases use numeric columns, so SkBee's NBT support is unnecessary.
            }

            val downloadSkriptReflectForBenchmark by tasks.registering(DownloadFile::class) {
                description = "Downloads skript-reflect for nanosecond benchmark timing."
                group = "verification"
                url.set("https://github.com/SkriptLang/skript-reflect/releases/download/v$skriptReflectVersion/skript-reflect-$skriptReflectVersion.jar")
                target.set(skriptReflectJar)
            }

            val serverBenchmark by tasks.registering(ReportSkriptServerBenchmark::class) {
                description = "Reports the tick cost of one write and one read, measured by Skript on a real server."
                group = "verification"

                dependsOn(runServerBenchmark)
                serverLog.set(serverBenchmarkDirectory.map { it.file("logs/latest.log") })
                resultsFile.set(serverBenchmarkResults)
                backend.set(benchmarkDatabaseType.map { if (it == "JDBC") "SQLite" else it })
            }
        }
    }
}
