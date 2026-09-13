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
rootProject.name = "xiaojie-orm"
include("mongodb-implementation")
include("generic-jdbc-implementation")
include("core")
include("postgresql-implementation")
include("rocksdb-implementation")
