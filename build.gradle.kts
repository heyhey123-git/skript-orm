import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.run.paper)
}

// Read from the catalog because the shaded jar's relocation prefixes embed both versions.
val kotlinVersion = libs.versions.kotlin.get()
val kotlinCoroutinesVersion = libs.versions.coroutines.get()
val shadePrefix = "io.github.heyhey123.xiaojieorm.libs"

// -PbundleModules=mod1,mod2
val bundledModules: List<String> = run {
    val raw = providers.gradleProperty("bundleModules").orNull
    raw
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.distinct()
        ?.filter {
            val exists = project.findProject(":$it") != null
            if (!exists) logger.warn("忽略不存在的子模块: $it")
            exists
        }
        .takeIf { !it.isNullOrEmpty() }
        ?: listOf("generic-jdbc-implementation") // 默认值
}

// 确保先评估这些子模块
bundledModules.forEach { evaluationDependsOn(":$it") }

allprojects {
    // Read from gradle.properties rather than repeated per module, so the module jars, the shaded
    // jar and the `version` in plugin.yml cannot disagree. `.get()` fails the build loudly if the
    // property is ever deleted, instead of silently versioning everything `unspecified`.
    group = providers.gradleProperty("group").get()
    version = providers.gradleProperty("version").get()

    repositories {
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.skriptlang.org/releases")
        maven("https://repo.destroystokyo.com/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-public/")
    }
}

// Type-safe catalog accessors belong to the script's own project, so they cannot be used inside the
// `subprojects { }` block below. Reading them once here keeps that block free of catalog API noise.
val paperApi = libs.paper.api
val skriptLibrary = libs.skript
val nbtApiLibrary = libs.nbt.api
val coroutinesCore = libs.coroutines.core

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    // Formatting and import rules come from the root .editorconfig, so IntelliJ and the
    // command line agree without duplicating them here.
    //   ./gradlew ktlintCheck   reports violations; ktlintCheck is wired into check.
    //   ./gradlew ktlintFormat  fixes everything that is mechanically fixable.
    // The plugin defaults to the ktlint version it ships with. To pin one, or to report
    // without failing the build, configure the extension:
    //   configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    //       version.set("1.7.1")
    //       ignoreFailures.set(true)
    //   }
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    dependencies {
        compileOnly(paperApi)
        compileOnly(kotlin("stdlib"))
        compileOnly(coroutinesCore)
        compileOnly(skriptLibrary)
        compileOnly(nbtApiLibrary)
    }
    kotlin {
        jvmToolchain(25)
    }
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(kotlin("stdlib"))
    implementation(libs.coroutines.core)
    implementation(project(":core"))
    bundledModules.forEach { implementation(project(":$it")) }
}

tasks {
    base {
        archivesName.set("xiaojieorm")
    }

    withType<ShadowJar> {
        // Include outputs from bundled modules
        bundledModules.forEach { module ->
            from(project(":$module").sourceSets.main.get().output)
            dependsOn(":$module:classes")
        }

        dependsOn(bundledModules.map { ":$it:jar" })

        // Relocate dependencies to avoid conflicts
        val kotlinEscapedVersion = kotlinVersion.filter { it != '.' }
        archiveAppendix.set("")
        archiveClassifier.set("")
        archiveVersion.set(version as String)
        destinationDirectory.set(file("$rootDir/build/dist"))

        // Kotlin
        relocate("kotlin.", "$shadePrefix.kotlin$kotlinEscapedVersion.")
        relocate("org.jetbrains.annotations.", "$shadePrefix.org.jetbrains.annotations2602.")
        relocate("org.intellij.", "$shadePrefix.org.intellij.")

        // kotlinx.coroutines
        val kotlinCoroutinesEscapedVersion = kotlinCoroutinesVersion.filter { it != '.' }
        relocate("kotlinx.coroutines.", "$shadePrefix.kotlinx.coroutines$kotlinCoroutinesEscapedVersion.")
        relocate("_COROUTINE.", "$shadePrefix._COROUTINE.")
        relocate("reactor.", "$shadePrefix.reactor.")
        relocate("org.reactivestreams.", "$shadePrefix.org.reactivestreams.")

        // JDBC
        relocate("com.zaxxer.hikari.", "$shadePrefix.com.zaxxer.hikari.")
        dependencies {
            exclude(dependency("org.slf4j:.*"))
        }

        relocate("org.postgresql.", "$shadePrefix.org.postgresql.")

        // MongoDB
        relocate("com.mongodb.", "$shadePrefix.com.mongodb.")
        relocate("org.bson.", "$shadePrefix.org.bson.")
    }

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(25)
}

// ---------------------------------------------------------------------------------------------
// The Skript server test
//
// `./gradlew serverTest` boots a real Paper server carrying Skript and this plugin, lets the
// scripts in `server-test/skript` drive the addon, and then checks the log the run produced. It is
// opt-in, like the other tests that need something from outside the JVM (CONTRIBUTION.md §8).
// ---------------------------------------------------------------------------------------------

// "26.2.build.124-stable" -> Minecraft version "26.2" and Paper build "124", so the server under
// test is the API line and the build this plugin compiles against, not merely a compatible one.
val paperCoordinates = libs.versions.paper.get().split(".build.")
require(paperCoordinates.size == 2) {
    "The `paper` entry in the version catalog must read '<minecraft>.build.<build>[-stable]'."
}
val paperMinecraftVersion = paperCoordinates[0]
val paperBuild = paperCoordinates[1].substringBefore('-').toInt()

val serverTestDirectory = layout.buildDirectory.dir("server-test")
val serverTestSource = layout.projectDirectory.dir("server-test")

// The server must load the same Skript the plugin compiles against, so this resolves that catalog
// entry instead of downloading a second copy from a plugin site.
val serverTestPlugins by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    "serverTestPlugins"(libs.skript)
    // The JDBC type registry resolves the NBT API when it initializes, so a server without this
    // plugin fails to connect with a missing `de.tr7zw.nbtapi.NBTCompound`. The test server installs
    // it because a real server has to.
    "serverTestPlugins"(libs.nbt.api)
}

// A database for the server test, read from the same keys the JDBC integration tests read, so one
// `-P` set configures both layers. Without a url the run has no database, which is the default and
// how it runs locally; with one, the element scripts run against it and a round trip joins them.
val serverTestDatabaseUrl = providers.gradleProperty("xiaojie.test.mysql.url")
    .orElse(providers.environmentVariable("XIAOJIE_TEST_MYSQL_URL"))
val serverTestDatabaseUsername = providers.gradleProperty("xiaojie.test.mysql.username")
    .orElse(providers.environmentVariable("XIAOJIE_TEST_MYSQL_USERNAME"))
    .orElse("root")
val serverTestDatabasePassword = providers.gradleProperty("xiaojie.test.mysql.password")
    .orElse(providers.environmentVariable("XIAOJIE_TEST_MYSQL_PASSWORD"))
    .orElse("")
val serverTestUsesDatabase = serverTestDatabaseUrl.isPresent

val prepareServerTest by tasks.registering {
    description = "Writes the run directory the Skript server test starts from."
    group = "verification"

    val runDirectory = serverTestDirectory
    val sourceDirectory = serverTestSource

    doLast {
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
        // The database mode adds a setup script and a round trip on top of the same element scripts,
        // and may replace one: it is copied over the tree above, so a file of the same name and path
        // wins. Only `elements/16-disconnect.sk` does, because it is the one element that would close
        // the connection the round trip is still using. The setup is generated because its
        // credentials come from the properties, not the repository.
        if (serverTestUsesDatabase) {
            sourceDirectory.dir("database").asFile.copyRecursively(scripts, overwrite = true)
            val setup = scripts.resolve("00-setup.sk")
            setup.writeText(
                setup.readText()
                    .replace("__URL__", serverTestDatabaseUrl.get())
                    .replace("__USERNAME__", serverTestDatabaseUsername.get())
                    .replace("__PASSWORD__", serverTestDatabasePassword.get())
            )
        }
        // The finisher is written rather than copied, because its count is the number of scripts that
        // report a detail line. Deriving it means adding an element cannot leave a stale number behind.
        val finisher = scripts.resolve("99-finish.sk")
        val reporting = scripts.walkTopDown()
            .filter { it.extension == "sk" && it != finisher }
            .count { "XIAOJIE_SELFTEST detail:" in it.readText() }
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
    pluginJars(tasks.shadowJar, serverTestPlugins)
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
val serverTestExpectedMessage = if (serverTestUsesDatabase) "" else "No database connected."
val serverTestChecks = buildMap {
    put("register table", serverTestExpectedMessage)
    // This one names an implementation that is not installed, so it reports the same in both modes.
    put("create connection", "Database 'NoSuchDatabase' is not supported.")
    put("insert one", serverTestExpectedMessage)
    put("insert many", serverTestExpectedMessage)
    put("insert one from variable", serverTestExpectedMessage)
    put("insert if absent", serverTestExpectedMessage)
    put("select one", serverTestExpectedMessage)
    put("select many", serverTestExpectedMessage)
    put("select page", serverTestExpectedMessage)
    put("select by id", serverTestExpectedMessage)
    put("delete entities", serverTestExpectedMessage)
    put("delete by id", serverTestExpectedMessage)
    put("update entities", serverTestExpectedMessage)
    put("update by id", serverTestExpectedMessage)
    put("upsert by id", serverTestExpectedMessage)
    // Disconnecting has no error channel: it proves it ran by the trigger reaching the end.
    put("disconnect", "ran")
    if (serverTestUsesDatabase) {
        put("setup", "")
        // What the setup's own read reported: empty means the table it registered is known to the
        // connection, and "Table ... not found." would mean registration did not take effect.
        put("setup select", "")
        put("roundtrip", "")
        // The round trip prints what each read returned, so a failure says whether the row exists as
        // the steps ran, whether it appears after a plain wait, and whether a `where` finds it.
        put("roundtrip now", "")
        put("roundtrip later", "")
        put("roundtrip by id", "")
    }
}

val serverTest by tasks.registering(VerifySkriptServerTest::class) {
    description = "Boots a Paper server with Skript and checks what this plugin did there."
    group = "verification"
    dependsOn(tasks.named("runServer"))
    serverLog.set(serverTestDirectory.map { directory -> directory.file("logs/latest.log") })
    expectedPluginVersion.set(version.toString())
    expectedSkriptVersion.set(libs.versions.skript.get())
    expectedChecks.set(serverTestChecks)
}

/**
 * Checks the log a `runServer` run wrote.
 *
 * The Skript side of the test reports what it did as `XIAOJIE_SELFTEST` lines, so the assertions
 * live here, where they can be read and changed without writing Skript.
 */
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

    @TaskAction
    fun checkLog() {
        val log = serverLog.get().asFile
        if (!log.isFile) {
            throw GradleException("The server wrote no log at ${log.absolutePath}.")
        }

        val lines = log.readLines()
        val problems = mutableListOf<String>()

        fun requireInLog(problem: String, text: String) {
            if (lines.none { text in it }) problems += "$problem\n      absent: $text"
        }

        requireInLog(
            "Paper never enabled this plugin, so the addon was not under test at all.",
            "Enabling xiaojie-orm v${expectedPluginVersion.get()}"
        )
        requireInLog(
            "The server did not run the Skript this plugin is built against.",
            "Enabling Skript v${expectedSkriptVersion.get()}"
        )
        requireInLog(
            "The self-test never reached its end, so a section it drives did not get through.",
            "XIAOJIE_SELFTEST=PASS"
        )

        val failures = lines.filter { "XIAOJIE_SELFTEST=FAIL" in it }
        if (failures.isNotEmpty()) {
            // One line each, because these lines are what CI annotates and annotations are capped.
            problems += failures.map { "The self-test reported: ${it.substringAfter("XIAOJIE_SELFTEST=").trim()}" }
        }

        // "XIAOJIE_SELFTEST detail: <element> -> <message>"
        val reported = lines
            .mapNotNull { line -> line.substringAfter("XIAOJIE_SELFTEST detail: ", "").ifEmpty { null } }
            .associate { line -> line.substringBeforeLast(" -> ") to line.substringAfterLast(" -> ") }

        expectedChecks.get().forEach { (element, message) ->
            val actual = reported[element]
            when {
                actual == null -> problems += "The self-test did not reach '$element'."
                // An empty expectation means the line only has to be there. The database mode cannot
                // predict how far each element got before the round trip states the real assertions.
                message.isEmpty() -> Unit
                actual != message -> problems += "'$element' reported '$actual' instead of '$message'."
            }
        }

        (reported.keys - expectedChecks.get().keys).forEach { unexpected ->
            problems += "The self-test reported '$unexpected', which this check does not know about."
        }

        // A statement Skript cannot match against any registered pattern is dropped, and the run
        // carries on looking healthy around it, so this is checked on its own.
        val unparsed = lines.filter { line -> line.contains("can't understand", ignoreCase = true) }
        if (unparsed.isNotEmpty()) {
            problems += "Skript could not parse part of a test script:\n" + indent(unparsed)
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
                    appendLine(indent(lines.filter { "XIAOJIE_SELFTEST" in it }.takeLast(40)))
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

