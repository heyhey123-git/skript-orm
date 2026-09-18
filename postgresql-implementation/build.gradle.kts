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
    compileOnly(project(":core"))
    implementation(project(":generic-jdbc-implementation"))
    implementation(libs.postgresql.driver)

    testImplementation(project(":core"))
    testImplementation(kotlin("test"))
    testImplementation(libs.coroutines.core)

    // Integration tests run this implementation against a real PostgreSQL server, which is the only
    // layer that can say whether the dialect's SQL is SQL PostgreSQL accepts. The driver comes with the
    // module; the rest is what a JVM without a server needs to build the objects the plugin builds:
    // Bukkit (MockBukkit supplies a server) for the values, and Skript for the classes its types name.
    //
    // SkBee is deliberately absent, as in the JDBC suite. It is the only NBT implementation the plugin
    // supports, so NBT support is unavailable in this JVM, which the round-trip tests record.
    "integrationTestImplementation"(platform(libs.testcontainers.bom))
    "integrationTestImplementation"(libs.testcontainers.postgresql)
    "integrationTestImplementation"(libs.testcontainers.junit)
    "integrationTestImplementation"(libs.mockbukkit)
    "integrationTestImplementation"(libs.slf4j.simple)
    "integrationTestImplementation"(libs.paper.api)
    "integrationTestImplementation"(libs.skript)
}

// Opt-in task: plain `test` stays fast and Docker-free. Run this explicitly, or from CI, when a change
// touches PostgreSQL SQL rendering or the JDBC layer underneath it.
val integrationTest by tasks.registering(Test::class) {
    description = "Runs the PostgreSQL integration tests against a real server."
    group = "verification"
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)

    // A Gradle property wins: `-P` is delivered with every invocation, even one that reuses a daemon
    // started before the environment was set, which is what makes a CI job deterministic. The system
    // property and the environment variables stay available for local runs.
    listOf(
        "skriptorm.test.postgres.url" to "SKRIPTORM_TEST_POSTGRES_URL",
        "skriptorm.test.postgres.username" to "SKRIPTORM_TEST_POSTGRES_USERNAME",
        "skriptorm.test.postgres.password" to "SKRIPTORM_TEST_POSTGRES_PASSWORD",
        "skriptorm.test.postgres.driver" to "SKRIPTORM_TEST_POSTGRES_DRIVER",
        "skriptorm.test.postgres.image" to "SKRIPTORM_TEST_POSTGRES_IMAGE"
    ).forEach { (property, environmentVariable) ->
        val value = providers.gradleProperty(property).orNull
            ?: providers.systemProperty(property).orNull
            ?: providers.environmentVariable(environmentVariable).orNull
        if (value != null) systemProperty(property, value)
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}
