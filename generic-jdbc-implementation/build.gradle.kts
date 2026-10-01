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

    // MariaDB is the one driver of the three types this module registers that the server does not already
    // have, so it is the only one declared here. Like every driver in this repository it is `compileOnly`:
    // the jar never carries it, and Paper downloads it from the `libraries` entry of plugin.yml instead.
    // Loading the class is what tells a server that never downloaded it apart from a wrong type name, so
    // the dependency has to be on the compile classpath and nowhere else.
    compileOnly(libs.mariadb.client)

    testImplementation(project(":core"))
    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.core)
    // An in-memory server, so what H2 reports for a column this plugin writes can be asked rather than
    // assumed. It is a driver and nothing more: no container, and it needs no Bukkit class, which is why
    // this one can live in the plain unit tests where the registry cannot be built.
    testImplementation(libs.h2)
    // The pool logs through SLF4J, which this module takes as compileOnly because the server provides it.
    // A test that builds a real HikariDataSource — the only place the pool's own behaviour, such as
    // evicting a connection, can be asserted — needs the API on the runtime classpath.
    testImplementation(libs.slf4j.api)

    // Integration tests run the JDBC implementation against a real MySQL or MariaDB server, so they
    // need both drivers and the connection pool at runtime. JdbcDataTypes also resolves the Bukkit and
    // Skript classes it declares when the object initializes, which is why those compileOnly
    // dependencies are repeated here: the integration test JVM is the plugin runtime without a server.
    //
    // SkBee is deliberately absent. It is the only NBT implementation the plugin supports, so NBT
    // support is unavailable in this JVM, which is what the round-trip tests record.
    "integrationTestImplementation"(platform(libs.testcontainers.bom))
    "integrationTestImplementation"(libs.testcontainers.mysql)
    "integrationTestImplementation"(libs.testcontainers.mariadb)
    "integrationTestImplementation"(libs.testcontainers.junit)
    // Supplies a Bukkit server, which the Bukkit-backed value types need before they can be built.
    // It cannot host Skript: MockBukkit loads a plugin as a generated subclass of its main class,
    // and Skript's main class is final.
    "integrationTestImplementation"(libs.mockbukkit)
    "integrationTestImplementation"(libs.mysql.connector)
    "integrationTestImplementation"(libs.mariadb.client)
    "integrationTestImplementation"(libs.slf4j.simple)
    "integrationTestImplementation"(libs.paper.api)
    "integrationTestImplementation"(libs.skript)
}

// Opt-in task: plain `test` stays fast and Docker-free. Run this explicitly, or from CI, when a
// change touches SQL generation, parameter binding, cursors, or the connection lifecycle.
val integrationTest by tasks.registering(Test::class) {
    description = "Runs the JDBC integration tests against a real MySQL or MariaDB server."
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
        "skriptorm.test.mysql.image" to "SKRIPTORM_TEST_MYSQL_IMAGE",
        "skriptorm.test.mariadb.url" to "SKRIPTORM_TEST_MARIADB_URL",
        "skriptorm.test.mariadb.username" to "SKRIPTORM_TEST_MARIADB_USERNAME",
        "skriptorm.test.mariadb.password" to "SKRIPTORM_TEST_MARIADB_PASSWORD",
        "skriptorm.test.mariadb.driver" to "SKRIPTORM_TEST_MARIADB_DRIVER",
        "skriptorm.test.mariadb.image" to "SKRIPTORM_TEST_MARIADB_IMAGE"
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
