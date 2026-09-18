plugins {
    kotlin("jvm")
}

// Declared before the dependency blocks: creating the source set also creates the
// integrationTestImplementation and integrationTestRuntimeOnly configurations they rely on.
val integrationTestSourceSet = sourceSets.create("integrationTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.coroutines.core)

    // The Skript parsers read Skript's own config node types, which the unit tests deliberately do
    // not have on their classpath. Skript needs Bukkit at class-loading time, so the API the plugin
    // compiles against is a real dependency here.
    //
    // MockBukkit supplies a Bukkit server, because Skript logs through Bukkit.getConsoleSender() and
    // NPEs without one. It is *not* used to load Skript or this addon: MockBukkit loads a plugin as
    // a generated subclass of its main class, and Skript's main class is final.
    "integrationTestImplementation"(libs.paper.api)
    "integrationTestImplementation"(libs.skript)
    "integrationTestImplementation"(libs.mockbukkit)
}

// Opt-in task: plain `test` stays fast and free of the Skript and Bukkit classpath.
val integrationTest by tasks.registering(Test::class) {
    description = "Runs the Skript integration tests against Skript's real classes."
    group = "verification"
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}

tasks.test {
    useJUnitPlatform()
}

// plugin.yml is a template. Bukkit reports the version in the "Enabling skript-orm v..." line and to
// `/version`, and Paper downloads the driver the `libraries` entry names; expanding both from the values
// the build already resolved keeps them from drifting away from what was built and tested.
tasks.processResources {
    // Declared as an input because the expansions below are not: without it Gradle sees unchanged
    // resources, skips the task, and the file keeps whatever version it was first written with — a
    // version bump in the catalog would silently not reach the jar.
    inputs.property("postgresqlDriver", libs.versions.postgresql.driver.get())

    filesMatching("plugin.yml") {
        expand(
            mapOf(
                "version" to version,
                "postgresqlDriver" to libs.versions.postgresql.driver.get()
            )
        )
    }
}

kotlin {
    jvmToolchain(25)
}
