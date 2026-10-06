package buildlogic

import org.gradle.api.artifacts.Configuration

data class ServerRuntime(
    val minecraftVersion: String,
    val paperBuild: Int,
    val plugins: Configuration
)
