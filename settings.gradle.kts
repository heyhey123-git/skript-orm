pluginManagement {
    repositories {
        // 优先使用镜像仓库，以兼容无法直连 Maven Central 的环境。
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "skript-orm"
include("mongodb-implementation")
include("generic-jdbc-implementation")
include("core")
include("postgresql-implementation")
// Keep benchmarks as a separate module. It is excluded from the bundled and published artifacts.
include("benchmarks")
