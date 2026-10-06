package buildlogic.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named

class ServerRuntimePlugin : Plugin<Project> {
    override fun apply(projectTarget: Project) {
        with(projectTarget) {
            pluginManager.apply("xyz.jpenilla.run-paper")
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            val paperCoordinates = libs.findVersion("paper").get().requiredVersion.split(".build.")
            require(paperCoordinates.size == 2) {
                "The paper version must read '<minecraft>.build.<build>[-stable]'."
            }
            val serverPlugins = configurations.create("serverTestPlugins") {
                isCanBeConsumed = false
                isCanBeResolved = true
            }
            dependencies { add(serverPlugins.name, libs.findLibrary("skript").get()) }
            extensions.add("serverRuntime", buildlogic.ServerRuntime(paperCoordinates[0], paperCoordinates[1].substringBefore('-').toInt(), serverPlugins))
        }
    }
}
