plugins { id("skript-orm.module") }

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

// Metadata uses the same bundle selection and catalog as the shaded jar.
val driverLibraries = buildlogic.driverLibraries(
    buildlogic.selectedModules(project),
    extensions.getByType<VersionCatalogsExtension>().named("libs")
)

tasks.processResources {
    // Both expansions are declared as inputs, because neither is: Gradle does not track the values of
    // `expand`, and `version` comes from `gradle.properties`, which is not a file any task watches. Without
    // these, a version bump leaves the task up to date and the jar is named for the new version while
    // `plugin.yml` inside it still reports the old one — which is exactly what happened the first time this
    // version was raised, and what the server test caught by looking for "Enabling skript-orm v1.1.0".
    inputs.property("version", version)
    inputs.property("driverLibraries", driverLibraries)

    filesMatching("plugin.yml") {
        expand(
            mapOf(
                "version" to version,
                "libraries" to driverLibraries
            )
        )
    }
}
