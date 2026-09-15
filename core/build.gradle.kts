plugins {
    kotlin("jvm")
}

group = "io.github.heyhey123"
version = "1.0-SNAPSHOT"

// Kept in step with the compileOnly versions declared for every subproject in the root build script.
val paperVersion = "26.2.build.+"
val skriptVersion = "2.13.2"
val nbtApiVersion = "2.15.5"
val mockBukkitVersion = "4.116.1"

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
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // The Skript parsers read Skript's own config node types, which the unit tests deliberately do
    // not have on their classpath. Skript needs Bukkit at class-loading time, so the API the plugin
    // compiles against is a real dependency here, and so is the NBT API that the type registry
    // resolves when it initializes.
    //
    // MockBukkit supplies a Bukkit server, because Skript logs through Bukkit.getConsoleSender() and
    // NPEs without one. It is *not* used to load Skript or this addon: MockBukkit loads a plugin as
    // a generated subclass of its main class, and Skript's main class is final.
    "integrationTestImplementation"("io.papermc.paper:paper-api:$paperVersion")
    "integrationTestImplementation"("com.github.SkriptLang:Skript:$skriptVersion")
    "integrationTestImplementation"("de.tr7zw:item-nbt-api-plugin:$nbtApiVersion")
    "integrationTestImplementation"("org.mockbukkit.mockbukkit:mockbukkit-v26.2:$mockBukkitVersion")
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
kotlin {
    jvmToolchain(25)
}
