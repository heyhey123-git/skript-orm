package buildlogic.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType

class ModulePlugin : Plugin<Project> {
    override fun apply(projectTarget: Project) {
        with(projectTarget) {
            pluginManager.apply("skript-orm.coordinates")
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("compileOnly", libs.findLibrary("paper-api").get())
                add("compileOnly", "org.jetbrains.kotlin:kotlin-stdlib:" + libs.findVersion("kotlin").get().requiredVersion)
                add("compileOnly", libs.findLibrary("coroutines-core").get())
                add("compileOnly", libs.findLibrary("skript").get())
            }

            extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> { jvmToolchain(25) }

            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
                // MockK's ByteBuddy agent attaches to the test JVM on JDK 25.
                jvmArgs("-Djdk.attach.allowAttachSelf=true", "-XX:+EnableDynamicAgentLoading")
            }
        }
    }
}
