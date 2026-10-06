plugins { id("skript-orm.module") }

// Performance work lives here rather than in a test, because a benchmark is not an assertion: it
// measures, it prints, and a red run only means the machine was busy.
//
// The module convention configures the JVM and formatting; this module is never bundled.
//
// The Gradle plugins that wrap JMH — `me.champeau.jmh` and JetBrains' kotlinx-benchmark — do not
// support this build's Gradle 9 yet, so JMH is wired by hand: the annotation processor generates the
// benchmark list from the Java sources, and the `jmh` task runs `org.openjdk.jmh.Main` against the
// module's own runtime classpath. Benchmarks are written in Java for the same reason the plugins are
// missing: JMH's generator processes Java sources, and Kotlin would need kapt in between.
//
// Kotlin declarations this suite measures are still reachable: `internal` is a Kotlin compiler rule,
// so an `internal object` with public members is a public class with public methods in the class file,
// and Java calls it directly. Members declared `internal` are the exception — the compiler renames
// those, and no benchmark here may call one.

dependencies {
    implementation(project(":core"))

    // The JDBC implementation, so a case can drive the path a script takes: register the factory, let
    // the plugin build its pool, and insert through the plugin's own query objects. A benchmark that
    // only touched `core` would measure a shape the plugin never runs in.
    implementation(project(":generic-jdbc-implementation"))

    // An in-process database, so the insert path can be measured here at all: the integration tests
    // reach MySQL, MariaDB and PostgreSQL through containers, and this machine has no container
    // runtime. H2 is already the server `H2SchemaTest` keeps the dialect's type names honest against,
    // and the plugin builds its own tables, which is the case H2's standard spellings are answered for.
    // Benchmark-only: this module is never shaded and never published.
    implementation(libs.h2)

    // Skript and Paper are `compileOnly` for every module, and some classes on this path are typed by
    // them (the data type registry names Skript's date and time), so they have to be present at run time.
    runtimeOnly(libs.paper.api)
    runtimeOnly(libs.skript)

    // The module convention adds Kotlin's standard library and coroutines as `compileOnly`,
    // because in a released jar they are shaded in and relocated; the `implementation` pair belongs to
    // the root project that builds that jar. This module is never shaded, so the benchmark JVM has to
    // bring its own: without these the first Kotlin class touched dies with
    // `NoClassDefFoundError: kotlin/jvm/internal/Intrinsics` inside the JMH fork.
    runtimeOnly(kotlin("stdlib"))
    runtimeOnly(libs.coroutines.core)

    implementation(libs.jmh.core)
    annotationProcessor(libs.jmh.generator)
}

// JDK 23 and later stopped running annotation processors that javac discovers implicitly; an explicit
// processor path is passed here, which is what re-enables them. The flag is spelled out because the
// toolchain is what decides this, not the build script.
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-proc:full")
}

// `-Pbenchmarks.filter=<regex>` narrows a run to matching benchmark names, and `-Pbenchmarks.forks=1`
// makes a local run quick. Neither has a default that changes what a case measures.
val benchmarksFilter: Provider<String> = providers.gradleProperty("benchmarks.filter")
val benchmarksForks: Provider<String> = providers.gradleProperty("benchmarks.forks").orElse("2")
val jmhResults = layout.buildDirectory.file("benchmarks/results.json")

// A case that counts what the plugin asked a driver for has to say so somewhere a build can read:
// JMH forks a JVM, so those counters die with the fork. The path is passed in rather than guessed,
// because the forked JVM's working directory is not something a case should have to assume.
val jmhCounters = layout.buildDirectory.file("benchmarks/insert-many-counters.txt")

val jmh by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Runs the JMH benchmarks and writes build/benchmarks/results.json."

    mainClass.set("org.openjdk.jmh.Main")
    classpath = sourceSets.main.get().runtimeClasspath
    outputs.file(jmhResults)
    outputs.file(jmhCounters)
    systemProperty("benchmarks.countersFile", jmhCounters.get().asFile.absolutePath)

    // JSON, because `benchmark-action/github-action-benchmark` reads JMH's own report format: the
    // history it keeps and the chart it draws need no conversion step of ours.
    argumentProviders.add(
        CommandLineArgumentProvider {
            val results = jmhResults.get().asFile.absolutePath
            buildList {
                add("-rf")
                add("json")
                add("-rff")
                add(results)
                add("-f")
                add(benchmarksForks.get())
                add("-wi")
                add("2")
                add("-i")
                add("3")
                benchmarksFilter.orNull?.let { add(it) }
            }
        }
    )

    doFirst {
        val results = jmhResults.get().asFile
        results.parentFile.mkdirs()
        logger.lifecycle("Writing {}", results)
    }
}
