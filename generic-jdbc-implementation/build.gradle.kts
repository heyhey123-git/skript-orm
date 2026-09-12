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
    api("com.zaxxer:HikariCP:7.0.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
