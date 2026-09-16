plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":core"))
    implementation(project(":generic-jdbc-implementation"))
    implementation(libs.postgresql.driver)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}
