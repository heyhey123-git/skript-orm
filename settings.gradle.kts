plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "xiaojie-orm"
include("mongodb-implementation")
include("generic-jdbc-implementation")
include("core")
include("postgresql-implementation")
include("rocksdb-implementation")
