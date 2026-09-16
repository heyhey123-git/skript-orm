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
}

// One entry per element the self-test drives: the name it reports, and the message it reports with
// it. Every section stops at the database lookup, because no database is connected, and says so
// through `last database error`. Keeping the list here rather than in the script means a section
// added to one without the other fails the test instead of passing unnoticed.
val serverTestChecks = mapOf(
    "register table" to "No database connected.",
    "create connection" to "Database 'NoSuchDatabase' is not supported.",
    "insert one" to "No database connected.",
    "insert many" to "No database connected.",
    "insert one from variable" to "No database connected.",
    "insert if absent" to "No database connected.",
    "select one" to "No database connected.",
    "select many" to "No database connected.",
    "select page" to "No database connected.",
    "select by id" to "No database connected.",
    "delete entities" to "No database connected.",
    "delete by id" to "No database connected.",
    "update entities" to "No database connected.",
    "update by id" to "No database connected.",
    "upsert by id" to "No database connected.",
    // Disconnecting has no error channel: it proves it ran by the trigger reaching the end.
    "disconnect" to "ran"
)

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
            problems += "The self-test reported a failure of its own:\n" + indent(failures)
        }

        // "XIAOJIE_SELFTEST detail: <element> -> <message>"
        val reported = lines
            .mapNotNull { line -> line.substringAfter("XIAOJIE_SELFTEST detail: ", "").ifEmpty { null } }
            .associate { line -> line.substringBeforeLast(" -> ") to line.substringAfterLast(" -> ") }

        expectedChecks.get().forEach { (element, message) ->
            val actual = reported[element]
            if (actual == null) {
                problems += "The self-test did not reach '$element'."
            } else if (actual != message) {
                problems += "'$element' reported '$actual' instead of '$message'."
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

