plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":core"))
    implementation(platform(libs.mongodb.driver.bom))
    implementation(libs.mongodb.driver.kotlin.coroutine)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}
