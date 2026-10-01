// Performance work lives here rather than in a test, because a benchmark is not an assertion: it
// measures, it prints, and a red run only means the machine was busy.
//
// `java`, `org.jetbrains.kotlin.jvm` and ktlint are applied to every subproject by the root build, and
// nothing here is added to `bundledModules`, so this module is never shaded into the plugin jar and
// never published.
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

val jmh by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Runs the JMH benchmarks and writes build/benchmarks/results.json."

    mainClass.set("org.openjdk.jmh.Main")
    classpath = sourceSets.main.get().runtimeClasspath
    outputs.file(jmhResults)

    // JSON, because `benchmark-action/github-action-benchmark` reads JMH's own report format: the
    // history it keeps and the chart it draws need no conversion step of ours.
    argumentProviders.add(
        CommandLineArgumentProvider {
            val results = jmhResults.get().asFile.absolutePath
            buildList {
                add("-rf"); add("json")
                add("-rff"); add(results)
                add("-f"); add(benchmarksForks.get())
                add("-wi"); add("2")
                add("-i"); add("3")
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
