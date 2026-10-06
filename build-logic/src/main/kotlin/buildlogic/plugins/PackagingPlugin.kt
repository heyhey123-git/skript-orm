package buildlogic.plugins

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType

class PackagingPlugin : Plugin<Project> {
    override fun apply(projectTarget: Project) {
        with(projectTarget) {
            pluginManager.apply("skript-orm.coordinates")
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("com.gradleup.shadow")
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            val kotlinVersion = libs.findVersion("kotlin").get().requiredVersion
            val kotlinCoroutinesVersion = libs.findVersion("coroutines").get().requiredVersion
            val shadePrefix = "io.github.heyhey123.skriptorm.libs"
            val bundledModules = buildlogic.selectedModules(project)

            dependencies {
                add("compileOnly", libs.findLibrary("paper-api").get())
                add("implementation", "org.jetbrains.kotlin:kotlin-stdlib:" + libs.findVersion("kotlin").get().requiredVersion)
                add("implementation", libs.findLibrary("coroutines-core").get())
                add("implementation", project(":core"))
                bundledModules.forEach { add("implementation", project(":$it")) }
            }

            tasks {
                withType<ShadowJar> {
                    // Project runtime dependencies carry the bundled module jars.
                    // Relocate dependencies to avoid conflicts
                    val kotlinEscapedVersion = kotlinVersion.filter { it != '.' }
                    archiveAppendix.set("")
                    archiveClassifier.set("")
                    archiveVersion.set(version as String)
                    destinationDirectory.set(file("$rootDir/build/dist"))

                    // Kotlin
                    relocate("kotlin.", "$shadePrefix.kotlin$kotlinEscapedVersion.")
                    relocate("org.jetbrains.annotations.", "$shadePrefix.org.jetbrains.annotations2602.")
                    relocate("org.intellij.", "$shadePrefix.org.intellij.")

                    // kotlinx.coroutines
                    val kotlinCoroutinesEscapedVersion = kotlinCoroutinesVersion.filter { it != '.' }
                    relocate("kotlinx.coroutines.", "$shadePrefix.kotlinx.coroutines$kotlinCoroutinesEscapedVersion.")
                    relocate("_COROUTINE.", "$shadePrefix._COROUTINE.")
                    relocate("reactor.", "$shadePrefix.reactor.")
                    relocate("org.reactivestreams.", "$shadePrefix.org.reactivestreams.")

                    // Bundle HikariCP and relocate it to avoid conflicts with other plugins.
                    relocate("com.zaxxer.hikari.", "$shadePrefix.com.zaxxer.hikari.")

                    dependencies {
                        // Use the logging API provided by Paper.
                        exclude(dependency("org.slf4j:.*"))
                    }
                }

                named("build") {
                    dependsOn(named("shadowJar"))
                }
            }

            extensions.configure<org.gradle.api.plugins.BasePluginExtension> {
                archivesName.set("skriptorm")
            }

            extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> { jvmToolchain(25) }
        }
    }
}
