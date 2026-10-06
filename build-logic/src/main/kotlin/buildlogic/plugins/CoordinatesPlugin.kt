package buildlogic.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories

class CoordinatesPlugin : Plugin<Project> {
    override fun apply(projectTarget: Project) {
        with(projectTarget) {
            group = providers.gradleProperty("group").get()
            version = providers.gradleProperty("version").get()

            repositories {
                maven("https://maven.aliyun.com/repository/central")
                maven("https://maven-central.storage-download.googleapis.com/maven2/")
                maven("https://repo.papermc.io/repository/maven-public/")
                maven("https://repo.skriptlang.org/releases")
                maven("https://repo.destroystokyo.com/repository/maven-public/")
                maven("https://repo.codemc.io/repository/maven-public/")
            }
        }
    }
}
