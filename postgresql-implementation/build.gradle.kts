plugins {
    kotlin("jvm")
}

group = "io.github.heyhey123"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(project(":core"))
    implementation(project(":generic-jdbc-implementation"))
    implementation("org.postgresql:postgresql:42.7.11")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}
