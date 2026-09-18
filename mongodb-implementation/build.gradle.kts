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

// The integration tests read the module's own view of what it writes: the collection the auto-increment
// counters live in is internal, and a test that copied its name would be checking a copy.
kotlin.target.compilations.getByName("integrationTest")
    .associateWith(kotlin.target.compilations.getByName("main"))

dependencies {
    compileOnly(project(":core"))

    // The driver is never in the jar and never on a runtime classpath of this build: Paper resolves the
    // `libraries` entry of plugin.yml into the server's `libraries/` and puts it on this plugin's
    // classpath, so what the module needs from it is the classes to compile against. `compileOnly` says
    // exactly that, where `implementation` would put a copy on every runtime classpath and the jar would
    // then have to exclude it to keep from shading a second driver.
    compileOnly(libs.mongodb.driver.sync)

    // The connection tests build driver settings and read them back, which needs the driver and nothing
    // else: no server, no Docker. They are what pins how a script's properties become a connection. The
    // driver is declared here as well because `compileOnly` above reaches no test classpath, and the
    // integration tests inherit this one.
    testImplementation(project(":core"))
    testImplementation(kotlin("test"))
    testImplementation(libs.coroutines.core)
    testImplementation(libs.mongodb.driver.sync)

    // The type registry resolves the Bukkit and Skript classes its types are built on when it
    // initializes, so the unit tests that read it need those two on their classpath. Nothing here
    // builds a value or starts a server.
    testImplementation(libs.paper.api)
    testImplementation(libs.skript)

    // Integration tests run this implementation against a real MongoDB server, which is the only layer
    // that can say whether the filters, the conversions and the counts mean what they claim. The driver
    // comes from `testImplementation` above; the rest is what a JVM without a server needs to build the
    // objects the plugin builds: Bukkit and Skript for the classes the type registry names when it
    // initializes.
    //
    // SkBee is deliberately absent, as in the JDBC and PostgreSQL suites. It is the only NBT
    // implementation the plugin supports, so NBT support is unavailable in this JVM.
    "integrationTestImplementation"(platform(libs.testcontainers.bom))
    "integrationTestImplementation"(libs.testcontainers.core)
    "integrationTestImplementation"(libs.slf4j.simple)
    "integrationTestImplementation"(libs.paper.api)
    "integrationTestImplementation"(libs.skript)
}

// Opt-in task: plain `test` stays fast and Docker-free. Run this explicitly, or from CI, when a change
// touches what the implementation sends to the server.
val integrationTest by tasks.registering(Test::class) {
    description = "Runs the MongoDB integration tests against a real server."
    group = "verification"
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)

    // A Gradle property wins: `-P` is delivered with every invocation, even one that reuses a daemon
    // started before the environment was set, which is what makes a CI job deterministic. The system
    // property and the environment variables stay available for local runs.
    listOf(
        "skriptorm.test.mongo.url" to "SKRIPTORM_TEST_MONGO_URL",
        "skriptorm.test.mongo.username" to "SKRIPTORM_TEST_MONGO_USERNAME",
        "skriptorm.test.mongo.password" to "SKRIPTORM_TEST_MONGO_PASSWORD",
        "skriptorm.test.mongo.database" to "SKRIPTORM_TEST_MONGO_DATABASE",
        "skriptorm.test.mongo.image" to "SKRIPTORM_TEST_MONGO_IMAGE"
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
