pluginManagement {
    repositories {
        // 当前网络访问 Maven Central 会返回 403，优先使用可访问的镜像。
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "skript-orm"
include("mongodb-implementation")
include("generic-jdbc-implementation")
include("core")
include("postgresql-implementation")
// Benchmarks are neither bundled nor published: the root build only carries the modules named in
// `bundledModules`. The module exists so that performance work has somewhere to live that is not a
// test, and so that the numbers in the documentation have a command that reproduces them.
include("benchmarks")
