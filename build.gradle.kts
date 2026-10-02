import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.FileNotFoundException
import java.io.IOException
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

// Which implementation modules the jar carries. `-PbundleModules=mod1,mod2` builds a jar for a
// combination that is not the default, which is how a developer uses an implementation that is not
// released yet, or none of them.
//
// The drivers those modules need are not modules and are never shaded: Paper downloads the `libraries`
// entry of plugin.yml into the server's `libraries/` on the first start, and puts it on this plugin's
// classpath. See the "What is inside the jar" section of docs/compatibility.md.
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
        ?: listOf(
            "generic-jdbc-implementation",
            "postgresql-implementation",
            "mongodb-implementation"
        ) // 默认值
}

// Nothing here excludes a driver, because no driver reaches this classpath: an implementation module
// declares its driver `compileOnly`, so the only copy of it is the one Paper downloads from the
// `libraries` entry of plugin.yml into the server's `libraries/` on the first start. A driver declared as
// a normal dependency would be shaded into the jar instead, and a rewritten copy beside the downloaded
// one would be two drivers.

// The `libraries` entry plugin.yml is built from: one driver for each implementation the jar carries, in
// the YAML shape the file wants. A driver for an implementation that is not in the jar would be a
// download nobody asked for, and a download that fails stops the plugin from loading at all — so the list
// follows the modules rather than the catalog.
//
// Handed to `:core`, which expands plugin.yml, instead of letting it work the list out again: the modules
// this jar carries are decided here.
val driverLibraries: String = buildList {
    if ("generic-jdbc-implementation" in bundledModules) {
        // MariaDB's driver only. MySQL's is the one the server already has, and the generic type is for a
        // driver this jar knows nothing about, so neither belongs in a download list.
        add("org.mariadb.jdbc:mariadb-java-client:${libs.versions.mariadb.client.get()}")
    }
    if ("postgresql-implementation" in bundledModules) {
        add("org.postgresql:postgresql:${libs.versions.postgresql.driver.get()}")
    }
    if ("mongodb-implementation" in bundledModules) {
        add("org.mongodb:mongodb-driver-sync:${libs.versions.mongodb.driver.get()}")
    }
}.let { libraries ->
    // The empty case is a flow sequence rather than nothing, so the entry stays a YAML list of no entries
    // instead of a key with no value at all.
    if (libraries.isEmpty()) " []" else "\n" + libraries.joinToString(separator = "\n") { "    - $it" }
}

project(":core").extensions.extraProperties["skriptOrmDriverLibraries"] = driverLibraries

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

    // MockK instruments the test JVM through ByteBuddy. ByteBuddy tries the virtual machine
    // interface, then attaching to its own process, and only then falls back to starting a second
    // JVM to attach for it. That last fallback fails wherever a process may not be started or may
    // not read another process's command line, and the failure surfaces as a bare
    // `NoClassDefFoundError: JvmMockKGateway` in every test that uses MockK.
    //
    // Both flags belong to the self-attach path, which needs nothing from outside its own JVM: the
    // JDK declines to attach a VM to itself without the first, and since JDK 21 it also declines to
    // load an agent into a running VM without the second.
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("-Djdk.attach.allowAttachSelf=true", "-XX:+EnableDynamicAgentLoading")
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

        // The connection pool is shaded and relocated: nothing else on a server provides it, and a second
        // HikariCP would be a second answer to where connections come from.
        relocate("com.zaxxer.hikari.", "$shadePrefix.com.zaxxer.hikari.")

        dependencies {
            // The one thing that is excluded rather than relocated: the server already has slf4j, and a
            // shaded copy would be a second logging API on this plugin's classpath.
            exclude(dependency("org.slf4j:.*"))
        }
    }

    build {
        dependsOn(shadowJar)
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

// A database for the server test, read from the same keys the integration suite of that implementation
// reads, so one `-P` set configures both layers. Which implementation is `skriptorm.test.server.type`,
// the name the scripts write after `database`: `MySQL` by default, because that is what these scripts
// were written against and what the released jar registers beside `"JDBC"`, or `PostgreSQL` or
// `MongoDB`, which is what their own jobs run. Without a url the run has no database, which is the
// default and how it runs locally; with one, the element scripts run against it and a round trip joins
// them.

/**
 * What the server test needs to know about one implementation.
 *
 * @property keys the name its properties and environment variables are built from, which is not always
 *   the name a script writes: PostgreSQL's are `skriptorm.test.postgres.*`, not `postgresql`.
 * @property module the build module that registers its type name, when that is not part of the jar the
 *   run installs, so the test can say which one to bundle.
 * @property username the account a fresh install of that server has.
 * @property transactions whether a `database transaction` section can open one. Where it cannot, the
 *   section refuses, skips its body and carries on, so the run reaches the same scripts with different
 *   messages instead of different scripts.
 */
data class ServerTestImplementation(
    val keys: String,
    val module: String?,
    val username: String,
    val transactions: Boolean = true
)

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

// What a section reports where the implementation has no transactions. The wording is core's, the one
// `Database.beginTransaction` refuses with, so this is a copy of a message rather than of a decision.
val serverTestNoTransactionsMessage = "This database implementation does not support transactions."
// The names only a run with a database reaches, in a map of their own: the scripts that report them are
// copied only when a database is configured, while the consistency check below has to know them in both
// modes.
val serverTestDatabaseChecks = mapOf(
    // The all-form, driven by the disconnect element once nothing else needs a connection.
    "disconnect all" to "ran",
    "setup" to "",
    // What the setup's own read reported: empty means the table it registered is known to the
    // connection, and "Table ... not found." would mean registration did not take effect.
    "setup select" to "",
    "roundtrip" to "",
    // The round trip prints what each read returned, so a failure says whether the row exists as the
    // steps ran, whether it appears after a plain wait, and whether a `where` finds it.
    "roundtrip now" to "",
    "roundtrip later" to "",
    "roundtrip by id" to "",
    // The same read through a literal id, which is the one `%object%` slot a script can fill with a
    // value Skript has not typed yet.
    "roundtrip literal id" to "",
    // Named connections. Only the named connection registered `orm_secondary`, so the read outside the
    // scope has to report the table lookup failing while the scoped one succeeds: that difference is
    // what proves the scope chose a connection at all.
    "connections create" to "",
    "connections register" to "",
    "connections write" to "",
    "connections unscoped" to "Table 'orm_secondary' not found.",
    "connections scoped" to "",
    "connections default" to "",
    // `disconnect` without a name closes the connection in effect. The two lines after it say what it
    // closed: the default still answers for its own table, and the named connection is gone.
    "connections disconnect" to "ran",
    "connections default after" to "",
    // The refusal has two shapes, and which one is written depends on whether any connection is named at
    // that moment — a fact belonging to whichever other script has run by then, not to this one. What this
    // script owns is that the name is reported as unknown, so that much is checked and the rest is left to
    // `ConnectionScopeTest`, which pins both shapes directly.
    "connections gone" to "No connection named 'secondary'.",
    // `use connection` switches for the rest of the event, and the connection it names has no table of
    // its own: the lookup failing is what shows the switch reached it. The named disconnect then closes
    // that same connection, whatever the switch says.
    "connections create tertiary" to "",
    "connections used" to "Table 'orm_roundtrip' not found.",
    "connections named disconnect" to "ran",
    "connections tertiary gone" to "No connection named 'tertiary'.",
    // Transactions. `row1` is the row the first transaction committed and `row2` is `<none>` as long as
    // nothing else survived, so one line says both that a commit worked and that a rollback did.
    "transaction commit" to "",
    "transaction failed" to "Table 'orm_transaction_missing' not found.",
    "transaction rows" to "row1=committed row2=<none>",
    "transaction rollback" to "",
    "transaction rollback rows" to "row1=committed row2=<none>",
    "transaction timeout" to
        "The database transaction was open for longer than 2 seconds and was rolled back.",
    "transaction timeout rows" to "row1=committed row2=<none>",
    "transaction refuse" to
        "A table cannot be registered inside a database transaction, because creating it would commit that transaction."
)

// What those same transaction lines report on an implementation that has no transactions at all. The
// section refuses to open one, skips its body and carries on with the statement after it, so the script
// still reaches its end and reports the same names — with the refusal instead of a commit, and with no
// rows for the reads, since nothing the bodies would have written ran.
val serverTestNoTransactionChecks = mapOf(
    "transaction commit" to serverTestNoTransactionsMessage,
    "transaction failed" to serverTestNoTransactionsMessage,
    "transaction rows" to "row1=<none> row2=<none>",
    "transaction rollback" to serverTestNoTransactionsMessage,
    "transaction rollback rows" to "row1=<none> row2=<none>",
    "transaction timeout" to serverTestNoTransactionsMessage,
    "transaction timeout rows" to "row1=<none> row2=<none>",
    "transaction refuse" to serverTestNoTransactionsMessage
)

val serverTestChecks = buildMap {
    put("register table", serverTestExpectedMessage)
    // Re-registering the same table on the same connection succeeds and reports nothing, because that is
    // what reloading a script whose tables are declared in `on load` does. Without a database it stops at
    // the connection lookup first, exactly as the line above it does.
    put("register table again", serverTestExpectedMessage)
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
    // The generic `"JDBC"` type on the SQLite driver Paper carries. It reports its verdict as a word
    // rather than an error message, because it asserts what it read back and says `failed` when any of
    // it does not hold, in both modes.
    put("sqlite round trip", "ok")
    // A number written to a text column is written as its digits rather than refused: Skript cannot parse
    // a number as text, so the value is parsed as a number and written out here. It reports a word rather
    // than an error message for the same reason the round trip above does.
    put("sqlite number into string", "ok")
    // The raw statements on the same SQLite connection: their verdict is a word too, because they assert
    // both what the server returned and that each guard refused before anything was sent. They run in
    // both modes for the same reason the round trip does — they need no database server.
    put("raw sql", "ok")
    // The row ceiling, also on SQLite: a read past it is refused, and a write past it is sent as several
    // statements so that every row is written. It fills its own table with more rows than the ceiling allows,
    // which is what the read half needs and what no other server in this suite is cheap enough to do.
    put("row ceiling", "ok")
    // Where a statement may be written and what a delete has to carry. The expectations are the measured
    // ones: on the generic `"JDBC"` type a delete is refused by the dialect whether or not the script
    // wrote a limit, while a statement inside an `if` runs and counts its row. The update case is here to
    // say whether that refusal is about deletes or about every write with an optional limit.
    put("delete unlimited", "error=Limited delete is not supported by this JDBC dialect. rowsAffected=<none>")
    put("delete filtered", "error=Limited delete is not supported by this JDBC dialect. rowsAffected=<none>")
    put("delete limited", "error=Limited delete is not supported by this JDBC dialect. rowsAffected=<none>")
    put("update unlimited", "error=Limited update is not supported by this JDBC dialect. rowsAffected=<none>")
    put("statement in if", "error=<none> rowsAffected=1")
    put("setup in if", "error=<none> rowsAffected=1")
    // A nested loop's own value, with nothing in the body that waits. The suffixes count from the
    // outermost loop inward: `-1` is the outer loop and `-2` the inner, which the two cases read in
    // opposite directions so the rule cannot hold by accident.
    put("nested loop suffixed", "last=outer=2 inner=3")
    put("nested loop values", "last=outer=3 inner=2")
    // The stress cases. The three write cases are one experiment over the 30 000-value budget: 12 000 rows of
    // one column and 4000 rows of six are inside it, 12 000 rows of six are not and are sent as several
    // statements. All three must store every row, and the count each case reads back out of the table is
    // asserted by index — `size of` over the same result reported 0 for a page that held 5000 rows.
    put("stress batch", "error=<none> valuesAsked=12000 rowsReported=12000 rowsPaged=12000")
    put("stress columns", "error=<none> valuesAsked=24000 rowsReported=4000 rowsPaged=4000")
    put("stress split", "error=<none> valuesAsked=72000 rowsReported=12000 rowsPaged=12000")
    // The declared line is the file backend's verdict, and only that. The same case failed on MySQL,
    // PostgreSQL and MongoDB with `rowsAffected=-1` in the run that first carried it (d0d86a6), and those
    // three are expected to keep failing it until the cancellation path is fixed: the case asks whether the
    // row the timed-out transaction inserted is still there, whether the connection can take a write
    // afterwards, and whether the key is free, and it prints all three answers. Do not relax this to a
    // prefix or a pattern to make those jobs green — the divergence is the finding.
    put(
        "stress cancel",
        "error=The database transaction was open for longer than 5 seconds and was rolled back. " +
            "inserted=1 found=0 readError=<none> fresh=1 freshError=<none> rowsAffected=1 sameError=<none>"
    )
    // Why `size of` answers zero for a stored result, with the controls that separate Skript's size query
    // from the plugin's storage: a flat list the script built reports its 20 values and loops 20 times, the
    // plugin's result and the same shape built by the script both report 0 and loop 0 times, and walking the
    // result's indices still counts 20 rows.
    // A range spelling such as `loop 1 to 20` is deliberately not part of it: Skript has no such loop, drops
    // the line, and the unparsed check above fails the build on it before this line is compared — which is
    // how that lead was settled. The case asserts the forms that do exist, with a control that reads the rows
    // without any loop at all.
    put(
        "result size",
        "built=20 flatLooped=20 mirror=0 mirrorLooped=0 read=0 readLooped=0 walked=20 local=0 copy=0 " +
            "fast=0 firstId=1 lastId=20 fastLastId=5000"
    )
    if (serverTestUsesDatabase) {
        putAll(serverTestDatabaseChecks)
        // A run whose implementation has no transactions reports the same lines as any other database
        // run, except for the transaction ones — so those are overridden, not the whole map. Replacing it
        // is what the first version of this did, and the MongoDB job's annotations said so within a minute
        // of the run: `setup`, `roundtrip now` and the connection lines had all been reported and were
        // "not known to this check".
        if (!serverTestImplementation.transactions) {
            putAll(serverTestNoTransactionChecks)
        }
    }
}

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
    expectedSkriptVersion.set(libs.versions.skript.get())
    expectedChecks.set(serverTestChecks)
    undeclaredChecks.set(serverTestUndeclaredChecks)
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

// ---------------------------------------------------------------------------------------------
// The tick benchmark
//
// `./gradlew serverBenchmark` boots the same Paper server with the same plugin jar as `serverTest`, runs
// the scripts under `server-benchmark/skript`, and reads the numbers they logged. It is a separate run
// directory and a separate script directory on purpose: the checker above demands a declared expectation
// for every name reported under `server-test`, and a benchmark reports numbers, which an expectation
// cannot hold. Booting the server is not a second way of doing it — same task type, same properties file,
// same plugin jars — only the scripts and the reader differ.
//
// What this layer can and cannot see is in the benchmark scripts, where the measurement is made. In short:
// the wall clock is the script's own and is quantized by the tick a parked trigger resumes on, so the tick
// count and the longest tick inside the statement are what carry the cost; a percentile of tick durations
// over a server's life would need an observer that is not a script.
// ---------------------------------------------------------------------------------------------

val serverBenchmarkDirectory = layout.buildDirectory.dir("server-benchmark")
val serverBenchmarkSource = layout.projectDirectory.dir("server-benchmark")
// Where the medians are written as JSON, so the CI job can record them per CPU into the gh-pages history
// beside the JMH numbers. The root task writes to the root build directory; the JMH module's report lives
// in `benchmarks/build/benchmarks/` because it belongs to that module.
val serverBenchmarkResults = layout.buildDirectory.file("benchmarks/tick-results.json")

val prepareServerBenchmark by tasks.registering {
    description = "Writes the run directory the Skript benchmark starts from."
    group = "verification"

    val runDirectory = serverBenchmarkDirectory
    val sourceDirectory = serverBenchmarkSource
    // The same properties the self-test uses: same Paper, same flat world, same short boot.
    val propertiesFile = serverTestSource.file("server.properties")

    doLast {
        val run = runDirectory.get().asFile
        // Skript keeps its variables in its plugin directory, and a guard variable left over from an
        // earlier run would make the next one skip the work it was asked to do.
        run.resolve("plugins/Skript").deleteRecursively()
        val scripts = run.resolve("plugins/Skript/scripts")
        scripts.mkdirs()
        sourceDirectory.dir("skript").asFile.copyRecursively(scripts, overwrite = true)
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
    minecraftVersion(paperMinecraftVersion)
    build(paperBuild)
    runDirectory.set(serverBenchmarkDirectory)
    pluginJars(tasks.shadowJar, serverTestPlugins)
    // SkBee is deliberately absent. The self-test installs it so the NBT interop is exercised; the
    // benchmark drives `insert many` and `select many` against SQLite, which never reaches it.
}

/**
 * Reports the measurements the Skript benchmark logged, in the order they were made.
 *
 * A benchmark is not an assertion, so nothing here fails because a number is large: a slow measurement is
 * a fact about the machine that produced it, and the numbers are printed to be compared rather than
 * bounded. What does fail is a run that could not measure — the plugin never enabled, a script reported a
 * failure, or the log holds no measurement — because each of those ends in a green build with no evidence
 * behind it, which is the one outcome a benchmark must not produce.
 */
abstract class ReportSkriptServerBenchmark : DefaultTask() {

    /** The log of the server that just ran. */
    @get:Internal
    abstract val serverLog: RegularFileProperty

    /** Where the medians are written as JSON for the CI job to record into the gh-pages history. */
    @get:Internal
    abstract val resultsFile: RegularFileProperty

    @TaskAction
    fun report() {
        val log = serverLog.get().asFile
        if (!log.isFile) {
            throw GradleException("The benchmark server wrote no log at ${log.absolutePath}.")
        }
        val lines = log.readLines()

        val failures = lines.filter { "SKRIPTORM_BENCH=FAIL" in it }
        if (failures.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("The Skript benchmark could not measure what it was asked to:")
                    failures.forEach { appendLine("  - ${it.substringAfter("SKRIPTORM_BENCH=").trim()}") }
                }
            )
        }

        val loaded = lines.firstOrNull { "Enabling skript-orm" in it }
            ?: throw GradleException(
                "The benchmark server never enabled the plugin under test, so nothing in this run says " +
                    "anything about this plugin. Its log is at ${log.absolutePath}."
            )

        val measurements = lines.mapNotNull { parse(it) }
        if (measurements.none { it.kind == "write" }) {
            throw GradleException(
                buildString {
                    appendLine("The benchmark server ran but measured no write, so there is nothing to report.")
                    appendLine("Its log is at ${log.absolutePath}.")
                    // The scripts say what they handed each measurement, so a loop that produced nothing is
                    // reported rather than guessed at from ninety measurements that all say `<none>`.
                    val diagnostics = lines.filter { "SKRIPTORM_BENCH=DIAG" in it }
                    if (diagnostics.isNotEmpty()) {
                        appendLine("What the scripts said they were measuring:")
                        diagnostics.take(12).forEach {
                            appendLine("  ${it.substringAfter("SKRIPTORM_BENCH=").trim()}")
                        }
                    }
                }
            )
        }

        // The scripts count the whole table, because nothing in the benchmark deletes: the rows a
        // measurement added are the difference from the line before it. That difference is also what says
        // the statement did what it claims — a write it did not want to grow the table by its own size, a
        // read it did not want to grow the table at all — which is why a disagreement fails the run. A
        // number that measures a write that did not happen is not a measurement, however it is printed.
        // The count is cumulative, so a measurement's own rows are the difference from the line before it.
        // The accumulator is the previous count rather than the previous difference, which an earlier
        // version of this got wrong in a way that made every difference null and the check below vacuous.
        var counted: Int? = null
        val added = measurements.map { measurement ->
            val delta = if (counted == null || measurement.total == null) null else measurement.total - counted
            counted = measurement.total ?: counted
            delta
        }
        val short = measurements.zip(added).filter { (measurement, delta) ->
            val expected = if (measurement.kind.endsWith("write")) measurement.rows else 0
            delta != null && delta != expected
        }
        if (short.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("The table did not grow by what a measurement says it did, so what it timed is not what it says:")
                    short.forEach { (measurement, delta) ->
                        val expected = if (measurement.kind.endsWith("write")) "${measurement.rows} rows" else "no rows"
                        appendLine("  - ${measurement.kind} ${measurement.rows}: expected $expected added, the table grew by $delta")
                    }
                }
            )
        }

        val bootSeconds = lines.firstNotNullOfOrNull { line ->
            Regex("""Done \(([0-9.]+)s\)!""").find(line)?.groupValues?.get(1)
        }
        logger.lifecycle("The benchmark server enabled {}, and booted in {}s.", loaded.trim(), bootSeconds ?: "?")

        // The controls the scripts run before their warm pass. A read whose longest tick is a tick's own
        // length is only evidence if the observer can see a lengthened tick at all, so the scripts time a
        // counted loop of their own choosing and report both what it took and what the observer made of it.
        // The 5000-row read in the table below is what those controls are for: six columns of 5000 rows is
        // 30 000 values, the size the pages describe and one above the threshold at which a result is
        // attached as a tree rather than written value by value.
        val calibrations = lines.mapNotNull { parseCalibration(it) }
        if (calibrations.isNotEmpty()) {
            logger.lifecycle("")
            logger.lifecycle("Controls, whose size the script chose instead of measuring:")
            logger.lifecycle("  control       spun       spin       window   longest tick     overrun")
            calibrations.forEach { control ->
                val overrun = control.gap?.let { (it - TICK_MILLIS).coerceAtLeast(0.0) }
                logger.lifecycle(
                    "  %-11s %6s %10s %12s %14s %10s".format(
                        control.label,
                        control.spun?.toString() ?: "-",
                        milliseconds(control.spin, control.spinRaw),
                        milliseconds(control.window, null),
                        milliseconds(control.gap, control.gapRaw),
                        overrun?.let { "%.2f ms".format(it) } ?: "unreadable"
                    )
                )
            }
            logger.lifecycle(
                "  A spin is a counted loop with no statement and no wait inside it, so it holds the server " +
                    "thread for as long as `spin` says, and `spun` is how many times it ran: a loop the " +
                    "parser dropped reports `spun=0` instead of passing for a fast block. `idle6s` is the " +
                    "floor — six seconds of waiting with nothing else watched — and it has to read one tick " +
                    "exactly, or the observer is adding something of its own. An overrun the size of the " +
                    "spin is the observer seeing that block, which is what makes the floor of this " +
                    "measurement a tick: a spin smaller than one has nothing to lengthen and reports none " +
                    "whatever it cost. The reads at 5000 rows in the table below are of that size, so their " +
                    "zero overrun says they do not lengthen a tick, not that they are free."
            )
        }

        logger.lifecycle("")
        logger.lifecycle("One Skript statement, as the script that issued it saw it:")
        logger.lifecycle(
            "  kind      rows  ticks   wall clock  longest tick   overrun    wall us/v  overrun us/v          " +
                "plugin        total   added  stored"
        )
        measurements.zip(added).forEach { (measurement, delta) -> logger.lifecycle(format(measurement, delta)) }

        logger.lifecycle("")
        logger.lifecycle("Per size, taken as the middle of what this run measured:")
        measurements
            .groupBy { it.kind to it.rows }
            .toSortedMap(compareBy({ it.first }, { it.second }))
            .forEach { (size, group) -> logger.lifecycle(formatMedian(size.first, size.second, group)) }

        // The same medians as JSON, so the CI job can record them per CPU into the gh-pages history beside
        // the JMH numbers. The tick numbers are report-only and never gated — the reason lives in the
        // workflow where the recording happens — so this file is a record, not a verdict. One named point
        // per (kind, rows) rather than a single headline, because the harness emits a curve and a curve is
        // what gets recorded; a run that reports zero overhang keeps its zero.
        val results = resultsFile.get().asFile
        results.parentFile.mkdirs()
        fun medianValue(group: List<Measurement>, select: (Measurement) -> Double?): Double? {
            val values = group.mapNotNull(select).sorted()
            return if (values.isEmpty()) null else values[values.size / 2]
        }
        val tickEntries = mutableListOf<String>()
        measurements
            .groupBy { it.kind to it.rows }
            .toSortedMap(compareBy({ it.first }, { it.second }))
            .forEach { (size, group) ->
                val (kind, rows) = size
                medianValue(group) { it.wall }?.let { wall ->
                    tickEntries += """{"name":"$kind ${rows}rows wall","unit":"ms","value":$wall}"""
                }
                medianValue(group) { it.gap }?.let { gap ->
                    val overrun = (gap - TICK_MILLIS).coerceAtLeast(0.0)
                    tickEntries += """{"name":"$kind ${rows}rows overrun","unit":"ms","value":$overrun}"""
                }
            }
        results.writeText("[${tickEntries.joinToString(",")}]\n")
        logger.lifecycle("Recorded {} tick points to {}.", tickEntries.size, results.absolutePath)

        logger.lifecycle("")
        logger.lifecycle(
            "The wall clock is the script's own, read around one statement, and it cannot go below the tick " +
                "a parked trigger resumes on. Work shorter than a tick therefore shows in the longest-tick " +
                "column rather than in the wall clock, and the guard variables and fill loops around each " +
                "statement are outside it. A tick is 50 ms by the server's contract, and `overrun` is the " +
                "longest tick minus that: the part of a tick above a tick's own length, which is the " +
                "statement showing up on the server thread. The per-value figure beside it divides that " +
                "overrun by the statement's values, which attributes the whole overrun to the statement and " +
                "includes whatever this observer costs while it watches: it is a derivation from the measured " +
                "tick, not a measurement of its own, so a read that never stretched a tick reads as zero " +
                "there rather than as free. Skript writes a timespan to two decimals of a second, so the " +
                "clock, every gap and everything derived from them resolve to 10 ms: a tick's worth of work " +
                "under that is not visible here, and nothing in this report claims it is. `added` is the " +
                "difference in the table's own count between this measurement and the one before it, which " +
                "is the only number here that came from the database rather than from the script or the " +
                "plugin."
        )

        val disagreed = measurements.zip(added)
            .filter { (measurement, delta) -> measurement.affected != null && delta != null && measurement.affected != delta }
        if (disagreed.isNotEmpty()) {
            logger.lifecycle("")
            logger.lifecycle("The plugin's own count differed from the table for {} measurement(s):", disagreed.size)
            disagreed.forEach { (measurement, delta) ->
                logger.lifecycle(
                    "  {} {}: the plugin said {} and the table grew by {}",
                    measurement.kind, measurement.rows, measurement.affected, delta
                )
            }
        }

        logger.lifecycle("")
        logger.lifecycle("What the scripts wrote, unparsed, so the numbers above can be checked against it:")
        lines.filter {
            "SKRIPTORM_BENCH write:" in it || "SKRIPTORM_BENCH read:" in it ||
                "SKRIPTORM_BENCH warmwrite:" in it || "SKRIPTORM_BENCH warmread:" in it ||
                "SKRIPTORM_BENCH calibrate:" in it
        }
            .forEach { line -> logger.lifecycle("  {}", line.substringAfter("SKRIPTORM_BENCH ").trim()) }
    }

    private fun format(measurement: Measurement, added: Int?): String {
        val values = measurement.rows * COLUMNS
        val overrun = measurement.gap?.let { (it - TICK_MILLIS).coerceAtLeast(0.0) }
        return "  " + listOf(
            measurement.kind,
            "%6d rows".format(measurement.rows),
            "%4d ticks".format(measurement.ticks),
            measurement.wall?.let { "%9.2f ms".format(it) } ?: "%9s".format(describe(measurement.wallRaw)),
            measurement.gap?.let { "%9.2f ms".format(it) } ?: "%9s".format(describe(measurement.gapRaw)),
            overrun?.let { "%8.2f ms".format(it) } ?: "",
            measurement.wall?.let { "%11.1f".format(it * 1000.0 / values) } ?: "",
            overrun?.let { "%13.1f".format(it * 1000.0 / values) } ?: "",
            measurement.affected?.let { "affected %6d".format(it) } ?: "",
            measurement.total?.let { "%8d".format(it) } ?: "",
            added?.let { "%+7d".format(it) } ?: "",
            measurement.stored?.let { "stored %6d".format(it) } ?: "",
            measurement.error?.let { "error: $it" } ?: ""
        ).filter { it.isNotEmpty() }.joinToString("  ")
    }

    private fun formatMedian(kind: String, rows: Int, group: List<Measurement>): String {
        fun median(select: (Measurement) -> Double?): String {
            val values = group.mapNotNull(select).sorted()
            return if (values.isEmpty()) "unparsed" else "%9.2f".format(values[values.size / 2])
        }
        val ticks = group.map { it.ticks }.sorted()
        val affected = group.mapNotNull { it.affected }.sorted()
        val values = rows * COLUMNS
        return "  %-9s %6d rows  wall %s ms  longest tick %s ms  ticks %3d  wall %8.1f us/value  overrun %8.1f us/value  %s".format(
            kind,
            rows,
            median { it.wall },
            median { it.gap },
            ticks[ticks.size / 2],
            group.mapNotNull { it.wall }.sorted().let { if (it.isEmpty()) Double.NaN else it[it.size / 2] * 1000.0 / values },
            group.mapNotNull { it.gap }.sorted().let {
                if (it.isEmpty()) Double.NaN else (it[it.size / 2] - TICK_MILLIS).coerceAtLeast(0.0) * 1000.0 / values
            },
            if (affected.isEmpty()) "" else "affected ${affected[affected.size / 2]}"
        )
    }

    private fun parseCalibration(line: String): Calibration? {
        if ("SKRIPTORM_BENCH calibrate:" !in line) return null
        val spin = field(line, "spin")
        val gap = field(line, "gap")
        return Calibration(
            label = field(line, "label") ?: return null,
            spun = field(line, "spun")?.toIntOrNull(),
            spin = toMilliseconds(spin),
            spinRaw = spin,
            window = toMilliseconds(field(line, "window")),
            gap = toMilliseconds(gap),
            gapRaw = gap
        )
    }

    private fun parse(line: String): Measurement? {
        val kind = when {
            "SKRIPTORM_BENCH write:" in line -> "write"
            "SKRIPTORM_BENCH read:" in line -> "read"
            "SKRIPTORM_BENCH warmwrite:" in line -> "warmwrite"
            "SKRIPTORM_BENCH warmread:" in line -> "warmread"
            else -> return null
        }
        val rows = field(line, "rows")?.toIntOrNull() ?: return null
        val wall = field(line, "ms")
        val gap = field(line, "gap")
        return Measurement(
            kind = kind,
            rows = rows,
            ticks = field(line, "ticks")?.toIntOrNull() ?: -1,
            wall = toMilliseconds(wall),
            wallRaw = wall,
            gap = toMilliseconds(gap),
            gapRaw = gap,
            affected = field(line, "affected")?.toIntOrNull(),
            stored = field(line, "stored")?.toIntOrNull(),
            total = field(line, "total")?.toIntOrNull(),
            error = Regex("""error=(.*)$""").find(line)?.groupValues?.get(1)?.trim()
                ?.takeIf { it.isNotEmpty() && it != "<none>" }
        )
    }

    private fun field(line: String, name: String): String? =
        Regex("""\b${Regex.escape(name)}=(\[[^]]*]|\S+)""").find(line)?.groupValues?.get(1)

    /** A timespan as a number with its unit, or the script's own wording when the unit is unknown. */
    private fun milliseconds(value: Double?, raw: String?): String =
        value?.let { "%.2f ms".format(it) } ?: describe(raw)

    /** A timespan as the script wrote it, for a column whose number could not be read. */
    private fun describe(raw: String?): String = when (raw?.trim()) {
        null -> "-"
        "<none>" -> "none"
        else -> "unparsed"
    }

    /**
     * A timespan as Skript writes it — `0.05 seconds`, `240 milliseconds` — turned into milliseconds, or
     * null when the wording is one this does not know, so an unreadable unit is reported rather than
     * guessed at. A tick is 50 ms by the server's contract, so converting ticks is arithmetic, not a
     * measurement of this machine.
     */
    private fun toMilliseconds(raw: String?): Double? {
        val text = raw?.trim()?.removeSurrounding("[", "]")?.trim() ?: return null
        val match = Regex("""^([0-9]+(?:[.,][0-9]+)?)\s*(\S+)$""").find(text) ?: return null
        val value = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
        return when (match.groupValues[2].lowercase()) {
            "millisecond", "milliseconds", "ms" -> value
            "tick", "ticks" -> value * 50.0
            "second", "seconds" -> value * 1000.0
            "minute", "minutes" -> value * 60_000.0
            "hour", "hours" -> value * 3_600_000.0
            "day", "days" -> value * 86_400_000.0
            else -> null
        }
    }

    /**
     * One measurement the scripts reported.
     *
     * @property wall what the script's own clock said, in milliseconds, and [wallRaw] the same timespan in
     * Skript's own wording so an unreadable one is visible rather than lost.
     * @property gap the longest tick inside the statement's window, and [gapRaw] its wording as well.
     * @property affected the row count the plugin itself reported, which is compared with what the table
     * grew by rather than trusted: they are the same number only when the backend's count was exact.
     * @property stored what a read left in the variable it was told to store into, counted by index. A read
     * past the ceiling stores nothing, so this is where that refusal is visible.
     * @property total the rows in the whole table after the statement, which the reader differences.
     */
    /**
     * One control the scripts ran: a block of work whose size the script chose, and what the observer saw.
     *
     * @property spun how many times the loop ran, which is what keeps a loop the parser dropped from being
     * read as a block that was fast.
     * @property spin the loop's own duration as the script's clock read it. Nothing waits inside the loop,
     * so this is the one duration in the report that is not quantized by a parked trigger resuming.
     * @property window the whole watched window, which is the spin plus the wait that follows it, so the
     * observer has a tick boundary to report the spin's tick at.
     * @property gap the longest tick the observer saw inside that window, and [gapRaw] its own wording.
     */
    private data class Calibration(
        val label: String,
        val spun: Int?,
        val spin: Double?,
        val spinRaw: String?,
        val window: Double?,
        val gap: Double?,
        val gapRaw: String?
    )

    private data class Measurement(
        val kind: String,
        val rows: Int,
        val ticks: Int,
        val wall: Double?,
        val wallRaw: String?,
        val gap: Double?,
        val gapRaw: String?,
        val affected: Int?,
        val stored: Int?,
        val total: Int?,
        val error: String?
    )

    private companion object {
        /** The benchmark table's width, which is what makes 5000 rows one 30000-value statement. */
        const val COLUMNS = 6

        /** A tick's own length, so an overrun can be told from a tick that did its normal work. */
        const val TICK_MILLIS = 50.0
    }
}

val serverBenchmark by tasks.registering(ReportSkriptServerBenchmark::class) {
    description = "Reports the tick cost of one write and one read, measured by Skript on a real server."
    group = "verification"

    dependsOn(runServerBenchmark)
    serverLog.set(serverBenchmarkDirectory.map { it.file("logs/latest.log") })
    resultsFile.set(serverBenchmarkResults)
}

// ---------------------------------------------------------------------------------------------
// The release notes
//
// `CHANGELOG.md` is where a release describes itself, and this is the one place those notes are taken out
// of it. `release.yml` publishes what this writes, and a person correcting a release that is already
// published runs the same task instead of writing a second extraction by hand — so the release body and
// the file cannot disagree. The section is looked up by the version in `gradle.properties`, which is the
// same version the jar is named with and the tag is made from.
// ---------------------------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------------------------
// The SkriptHub documentation JSON
//
// SkriptHub reads an addon's syntax from a JSON file that SkriptHubDocsTool generates on a running
// server, and its dashboard imports that file with a paste. `./gradlew gendocs` does the whole thing
// locally: it builds the jar, boots a disposable Paper server carrying Skript, this plugin and the
// tool, has that server generate the documentation, and leaves the result in `build/skripthub/`. It is
// a build output rather than a tracked file, because the annotations are the source and this is only
// what they generate. Nothing here belongs in CI either: the upload is a person pasting text, so the
// tool's version is pinned instead of floating.
// ---------------------------------------------------------------------------------------------

// SkriptHubDocsTool 1.17 wants Skript 2.15 or newer, and the server below carries the Skript this
// plugin is built against, so the two cannot drift apart unnoticed.
val skriptHubDocsToolVersion = "1.17"

// Kept in the Gradle user home rather than under build/, because the release workflow restores that
// directory between runs and this jar is the one part of the documentation run that comes off the network:
// a machine that has it once should never have to ask for it again, and the asking is what fails when
// GitHub's asset host answers a runner with 504.
val skriptHubDocsToolFile = gradle.gradleUserHomeDir.resolve(
    "caches/skript-hub-docs/skripthubdocstool-$skriptHubDocsToolVersion.jar"
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

/**
 * Checks the JSON the documentation tool wrote, and copies it out of the run directory.
 *
 * The file is generated from the annotations, so it is checked rather than trusted: an element that
 * lost a pattern, an example that would show an empty first line, or a version that disagrees with
 * `gradle.properties` would otherwise reach SkriptHub unnoticed. It is the server test's log check one
 * layer up.
 *
 * What is written out is that document with the blank lines around every example taken off; see
 * [withoutBlankEdges] for why the tool's copy carries them.
 */
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
        target.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(withoutBlankEdges(document))))
        logger.lifecycle(
            "{} entries written to {}, ready for SkriptHub's JSON import.",
            entries.size,
            target.relativeTo(project.projectDir)
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
    destination.set(layout.buildDirectory.file("skripthub/skript-orm.json"))
    expectedVersion.set(version.toString())
}

