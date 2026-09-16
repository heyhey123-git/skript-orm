plugins {
    kotlin("jvm")
}

group = "io.github.heyhey123"
version = "1.0-SNAPSHOT"

val testcontainersVersion = "1.21.3"
val mysqlConnectorVersion = "9.5.0"
val mockBukkitVersion = "4.116.1"

// Kept in step with the compileOnly versions declared for every subproject in the root build script.
val paperVersion = "26.2.build.+"
val skriptVersion = "2.13.2"
val nbtApiVersion = "2.15.5"

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
    api("com.zaxxer:HikariCP:7.0.2") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    compileOnly("org.slf4j:slf4j-api:2.0.17")

    testImplementation(project(":core"))
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Integration tests run the JDBC implementation against a real MySQL server, so they need the
    // driver and connection pool at runtime. JdbcDataTypes also resolves the Bukkit, NBT and Skript
    // classes it declares when the object initializes, which is why those compileOnly dependencies
    // are repeated here: the integration test JVM is the plugin runtime without a server.
    "integrationTestImplementation"("org.testcontainers:mysql")
    "integrationTestImplementation"("org.testcontainers:junit-jupiter")
    // Supplies a Bukkit server, which the Bukkit-backed value types need before they can be built.
    // It cannot host Skript: MockBukkit loads a plugin as a generated subclass of its main class,
    // and Skript's main class is final.
    "integrationTestImplementation"("org.mockbukkit.mockbukkit:mockbukkit-v26.2:$mockBukkitVersion")
    "integrationTestImplementation"("com.mysql:mysql-connector-j:$mysqlConnectorVersion")
    "integrationTestImplementation"("org.slf4j:slf4j-simple:2.0.17")
    "integrationTestImplementation"("io.papermc.paper:paper-api:$paperVersion")
    "integrationTestImplementation"("com.github.SkriptLang:Skript:$skriptVersion")
    "integrationTestImplementation"("de.tr7zw:item-nbt-api-plugin:$nbtApiVersion")
    "integrationTestImplementation"(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
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
        "xiaojie.test.mysql.url" to "XIAOJIE_TEST_MYSQL_URL",
        "xiaojie.test.mysql.username" to "XIAOJIE_TEST_MYSQL_USERNAME",
        "xiaojie.test.mysql.password" to "XIAOJIE_TEST_MYSQL_PASSWORD",
        "xiaojie.test.mysql.driver" to "XIAOJIE_TEST_MYSQL_DRIVER",
        "xiaojie.test.mysql.image" to "XIAOJIE_TEST_MYSQL_IMAGE"
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
