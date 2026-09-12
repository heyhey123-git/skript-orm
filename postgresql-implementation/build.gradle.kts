plugins {
    kotlin("jvm")
}

group = "io.github.heyhey123"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":core"))
    implementation(project(":generic-jdbc-implementation"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
