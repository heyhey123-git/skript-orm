import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.json.JsonSlurper
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
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
val shadePrefix = "io.github.heyhey123.skriptorm.libs"

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
        archivesName.set("skriptorm")
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
    // Skript is a declared dependency of this plugin, so the test server installs the version it is
    // built against. SkBee is not, and is not listed here: `runServer` downloads it from Modrinth
    // below, because the NBT interop only exists on a server that has SkBee, and installing the
    // standalone NBT API instead would hide that, as it did before.
    "serverTestPlugins"(libs.skript)
}

// A database for the server test, read from the same keys the JDBC integration tests read, so one
// `-P` set configures both layers. Without a url the run has no database, which is the default and
// how it runs locally; with one, the element scripts run against it and a round trip joins them.
val serverTestDatabaseUrl = providers.gradleProperty("skriptorm.test.mysql.url")
    .orElse(providers.environmentVariable("SKRIPTORM_TEST_MYSQL_URL"))
val serverTestDatabaseUsername = providers.gradleProperty("skriptorm.test.mysql.username")
    .orElse(providers.environmentVariable("SKRIPTORM_TEST_MYSQL_USERNAME"))
    .orElse("root")
val serverTestDatabasePassword = providers.gradleProperty("skriptorm.test.mysql.password")
    .orElse(providers.environmentVariable("SKRIPTORM_TEST_MYSQL_PASSWORD"))
    .orElse("")
val serverTestUsesDatabase = serverTestDatabaseUrl.isPresent

val prepareServerTest by tasks.registering {
    description = "Writes the run directory the Skript server test starts from."
    group = "verification"

    val runDirectory = serverTestDirectory
    val sourceDirectory = serverTestSource
    // Read before the task runs: inside `doLast` the script's own project is out of reach.
    val examplesDirectory = layout.projectDirectory.dir("docs/examples")

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
            // element opens a second connection of its own and needs the same credentials.
            val url = serverTestDatabaseUrl.get()
            val username = serverTestDatabaseUsername.get()
            val password = serverTestDatabasePassword.get()
            scripts.walkTopDown()
                .filter { it.extension == "sk" }
                .forEach { script ->
                    val text = script.readText()
                    if ("__URL__" !in text) return@forEach
                    script.writeText(
                        text.replace("__URL__", url)
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
    // The same statements written without a colon, which is what the effect forms are for. The names carry
    // `unfiltered` because the section forms keep their own elements, and a script records itself under a
    // name of its own.
    put("select one unfiltered", serverTestExpectedMessage)
    put("select many unfiltered", serverTestExpectedMessage)
    put("select page unfiltered", serverTestExpectedMessage)
    put("select by id unfiltered", serverTestExpectedMessage)
    put("insert one from variable without colon", serverTestExpectedMessage)
    put("insert many from variable without colon", serverTestExpectedMessage)
    put("insert if absent without colon", serverTestExpectedMessage)
    put("update by id without colon", serverTestExpectedMessage)
    put("upsert by id without colon", serverTestExpectedMessage)
    put("delete by id without colon", serverTestExpectedMessage)
    put("delete entities without colon", serverTestExpectedMessage)
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
        // The all-form, driven by the disconnect element once nothing else needs a connection.
        put("disconnect all", "ran")
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
        // Named connections. Only the named connection registered `orm_secondary`, so the read outside
        // the scope has to report the table lookup failing while the scoped one succeeds: that
        // difference is what proves the scope chose a connection at all.
        put("connections create", "")
        put("connections register", "")
        put("connections write", "")
        put("connections unscoped", "Table 'orm_secondary' not found.")
        put("connections scoped", "")
        put("connections default", "")
        // `disconnect` without a name closes the connection in effect. The two lines after it say what
        // it closed: the default still answers for its own table, and the named connection is gone.
        put("connections disconnect", "ran")
        put("connections default after", "")
        put("connections gone", "No connection named 'secondary', and no connection has been created yet.")
        // `use connection` switches for the rest of the event, and the connection it names has no table
        // of its own: the lookup failing is what shows the switch reached it. The named disconnect then
        // closes that same connection, whatever the switch says.
        put("connections create tertiary", "")
        put("connections used", "Table 'orm_roundtrip' not found.")
        put("connections named disconnect", "ran")
        put("connections tertiary gone", "No connection named 'tertiary', and no connection has been created yet.")
        // Transactions. `row1` is the row the first transaction committed and `row2` is `<none>` as long
        // as nothing else survived, so one line says both that a commit worked and that a rollback did.
        put("transaction commit", "")
        put("transaction failed", "Table 'orm_transaction_missing' not found.")
        put("transaction rows", "row1=committed row2=<none>")
        put("transaction rollback", "")
        put("transaction rollback rows", "row1=committed row2=<none>")
        put(
            "transaction timeout",
            "The database transaction was open for longer than 2 seconds and was rolled back."
        )
        put("transaction timeout rows", "row1=committed row2=<none>")
        put(
            "transaction refuse",
            "A table cannot be registered inside a database transaction, because creating it would commit that transaction."
        )
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
 * The Skript side of the test reports what it did as `SKRIPTORM_SELFTEST` lines, so the assertions
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

// ---------------------------------------------------------------------------------------------
// The SkriptHub documentation JSON
//
// SkriptHub reads an addon's syntax from a JSON file that SkriptHubDocsTool generates on a running
// server, and its dashboard imports that file with a paste. `./gradlew gendocs` does the whole thing
// locally: it builds the jar, boots a disposable Paper server carrying Skript, this plugin and the
// tool, has that server generate the documentation, and writes the result where the repository keeps
// it. None of it belongs in CI — the upload is a person pasting text — so the tool's version is
// pinned here rather than tracked.
// ---------------------------------------------------------------------------------------------

// SkriptHubDocsTool 1.17 wants Skript 2.15 or newer, and the server below carries the Skript this
// plugin is built against, so the two cannot drift apart unnoticed.
val skriptHubDocsToolVersion = "1.17"

val skriptHubDocsToolFile = layout.buildDirectory.file(
    "tools/skripthubdocstool-$skriptHubDocsToolVersion.jar"
)
val skriptHubDocsDirectory = layout.buildDirectory.dir("skript-hub")

/**
 * Downloads one file to a fixed place.
 *
 * The documentation tool is a server plugin published as a GitHub release asset, so it has no Maven
 * coordinates to ask a repository for. Naming the url is the whole task; the declared output is what
 * stops a second run from downloading it again.
 */
abstract class DownloadFile : DefaultTask() {

    @get:Input
    abstract val url: Property<String>

    @get:OutputFile
    abstract val target: RegularFileProperty

    @TaskAction
    fun download() {
        val file = target.get().asFile
        file.parentFile.mkdirs()
        // Downloaded beside the real name and then moved onto it, so an interrupted transfer is never
        // taken for the complete file by the next run.
        val partial = file.resolveSibling(file.name + ".part")
        URI(url.get()).toURL().openStream().use { input ->
            partial.outputStream().use { output -> input.copyTo(output) }
        }
        // Replacing is what makes a second download work: the file it moves onto is usually already
        // there, and a plain rename refuses to overwrite on Windows.
        Files.move(partial.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}

/**
 * Checks the JSON the documentation tool wrote, and copies it into the repository.
 *
 * The file is generated from the annotations, so it is checked rather than trusted: an element that
 * lost a pattern, an example that would show an empty first line, or a version that disagrees with
 * `gradle.properties` would otherwise reach SkriptHub unnoticed. It is the server test's log check one
 * layer up.
 */
abstract class CollectGendocs : DefaultTask() {

    /** What the tool wrote on the server that just ran. */
    @get:InputFile
    abstract val generated: RegularFileProperty

    /** Where the repository keeps the file that is pasted into SkriptHub. */
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
            "events", "conditions", "effects", "expressions", "types", "functions", "sections", "structures"
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
        target.writeText(source.readText())
        logger.lifecycle(
            "{} entries written to {}, ready for SkriptHub's JSON import.",
            entries.size,
            target.relativeTo(project.projectDir)
        )
    }
}

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
    pluginJars(tasks.shadowJar, serverTestPlugins, files(skriptHubDocsToolFile))
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
    destination.set(layout.projectDirectory.file("docs/skripthub/skript-orm.json"))
    expectedVersion.set(version.toString())
}

