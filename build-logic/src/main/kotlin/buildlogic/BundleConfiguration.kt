package buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog

/** Module selection shared by packaging, metadata expansion and server tests. */
fun selectedModules(project: Project): List<String> {
    val selected = project.providers.gradleProperty("bundleModules").orNull
        ?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.distinct()
        ?.filter { name ->
            val exists = project.rootProject.findProject(":$name") != null
            if (!exists) project.logger.warn("Ignoring unknown bundle module: {}", name)
            exists
        }
    return selected.takeUnless { it.isNullOrEmpty() }
        ?: listOf("generic-jdbc-implementation", "postgresql-implementation", "mongodb-implementation")
}

fun driverLibraries(modules: List<String>, libs: VersionCatalog): String {
    val libraries = buildList {
        if ("generic-jdbc-implementation" in modules) {
            add("org.mariadb.jdbc:mariadb-java-client:" + libs.findVersion("mariadb-client").get().requiredVersion)
        }
        if ("postgresql-implementation" in modules) {
            add("org.postgresql:postgresql:" + libs.findVersion("postgresql-driver").get().requiredVersion)
        }
        if ("mongodb-implementation" in modules) {
            add("org.mongodb:mongodb-driver-sync:" + libs.findVersion("mongodb-driver").get().requiredVersion)
        }
    }
    return if (libraries.isEmpty()) " []" else "\n" + libraries.joinToString("\n") { "    - $it" }
}
