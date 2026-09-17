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
    api(libs.hikari) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    compileOnly(libs.slf4j.api)

    testImplementation(project(":core"))
    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.core)

    // Integration tests run the JDBC implementation against a real MySQL server, so they need the
    // driver and connection pool at runtime. JdbcDataTypes also resolves the Bukkit and Skript classes
    // it declares when the object initializes, which is why those compileOnly dependencies are
    // repeated here: the integration test JVM is the plugin runtime without a server.
    //
    // SkBee is deliberately absent. It is the only NBT implementation the plugin supports, so NBT
    // support is unavailable in this JVM, which is what the round-trip tests record.
    "integrationTestImplementation"(platform(libs.testcontainers.bom))
    "integrationTestImplementation"(libs.testcontainers.mysql)
    "integrationTestImplementation"(libs.testcontainers.junit)
    // Supplies a Bukkit server, which the Bukkit-backed value types need before they can be built.
    // It cannot host Skript: MockBukkit loads a plugin as a generated subclass of its main class,
    // and Skript's main class is final.
    "integrationTestImplementation"(libs.mockbukkit)
    "integrationTestImplementation"(libs.mysql.connector)
    "integrationTestImplementation"(libs.slf4j.simple)
    "integrationTestImplementation"(libs.paper.api)
    "integrationTestImplementation"(libs.skript)
}

// Opt-in task: plain `test` stays fast and Docker-free. Run this explicitly, or from CI, when a
// change touches SQL generation, parameter binding, cursors, or the connection lifecycle.
val integrationTest by tasks.registering(Test::class) {
    description = "Runs the JDBC integration tests against a real MySQL server."
    group = "verification"
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)

    // A Gradle property wins: `-P` is delivered with every invocation, even one that reuses a daemon
    // started before the environment was set, which is what makes a CI job deterministic. `-D` and
    // the environment variables stay available for local runs.
    listOf(
        "skriptorm.test.mysql.url" to "SKRIPTORM_TEST_MYSQL_URL",
        "skriptorm.test.mysql.username" to "SKRIPTORM_TEST_MYSQL_USERNAME",
        "skriptorm.test.mysql.password" to "SKRIPTORM_TEST_MYSQL_PASSWORD",
        "skriptorm.test.mysql.driver" to "SKRIPTORM_TEST_MYSQL_DRIVER",
        "skriptorm.test.mysql.image" to "SKRIPTORM_TEST_MYSQL_IMAGE"
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
