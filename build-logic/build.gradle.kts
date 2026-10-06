plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

repositories {
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven-central.storage-download.googleapis.com/maven2/")
    gradlePluginPortal()
}

// Build plugins also run on developer machines whose Gradle JVM is older than the app's JDK 25.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
}

gradlePlugin {
    val conventions = mapOf(
        "coordinates" to "CoordinatesPlugin",
        "module" to "ModulePlugin",
        "packaging" to "PackagingPlugin",
        "server-runtime" to "ServerRuntimePlugin",
        "server-tests" to "ServerTestsPlugin",
        "server-benchmarks" to "ServerBenchmarksPlugin",
        "documentation" to "DocumentationPlugin",
        "release-notes" to "ReleaseNotesPlugin"
    )
    conventions.forEach { (name, implementation) ->
        plugins.register(name) {
            id = "skript-orm.$name"
            implementationClass = "buildlogic.plugins.$implementation"
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:" + libs.versions.kotlin.get())
    implementation("com.gradleup.shadow:shadow-gradle-plugin:" + libs.versions.shadow.get())
    implementation("org.jlleitschuh.gradle:ktlint-gradle:" + libs.versions.ktlint.get())
    implementation("xyz.jpenilla:run-task:" + libs.versions.run.paper.get())
}
