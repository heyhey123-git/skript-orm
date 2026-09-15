import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "9.2.2"
}


val kotlinVersion = "2.3.21"
val kotlinCoroutinesVersion = "1.10.2"
val paperVersion = "26.2.build.+"
val shadePrefix = "io.github.heyhey123.xiaojieorm.libs"

// -PbundleModules=mod1,mod2
val bundledModules: List<String> = run {
    val raw = providers.gradleProperty("bundleModules").orNull
    raw
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.distinct()
        ?.filter {
            val exists = project.findProject(":$it") != null
            if (!exists) logger.warn("忽略不存在的子模块: $it")
            exists
        }
        .takeIf { !it.isNullOrEmpty() }
        ?: listOf("generic-jdbc-implementation") // 默认值
}

// 确保先评估这些子模块
bundledModules.forEach { evaluationDependsOn(":$it") }


allprojects {
    group = "io.github.heyhey123"
    version = "1.0-SNAPSHOT"

    repositories {
        // 当前网络访问 repo.maven.apache.org 会返回 403；镜像优先，官方仓库保留为回退。
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.skriptlang.org/releases")
        maven("https://repo.destroystokyo.com/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-public/")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        compileOnly("io.papermc.paper:paper-api:$paperVersion")
        compileOnly(kotlin("stdlib"))
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
        compileOnly("com.github.SkriptLang:Skript:2.13.2")
        compileOnly("de.tr7zw:item-nbt-api-plugin:2.15.5")
    }
    kotlin {
        jvmToolchain(25)
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation(project(":core"))
    bundledModules.forEach { implementation(project(":$it")) }
}

tasks {
    base {
        archivesName.set("xiaojieorm")
    }

    withType<ShadowJar> {
        // Include outputs from bundled modules
        bundledModules.forEach { module ->
            from(project(":$module").sourceSets.main.get().output)
            dependsOn(":$module:classes")
        }

        dependsOn(bundledModules.map { ":$it:jar" })

        // Relocate dependencies to avoid conflicts
        val kotlinEscapedVersion = kotlinVersion.filter { it != '.' }
        archiveAppendix.set("")
        archiveClassifier.set("")
        archiveVersion.set(version as String)
        destinationDirectory.set(file("$rootDir/build/dist"))

        // Kotlin
        relocate("kotlin.", "$shadePrefix.kotlin${kotlinEscapedVersion}.")
        relocate("org.jetbrains.annotations.", "$shadePrefix.org.jetbrains.annotations2602.")
        relocate("org.intellij.", "$shadePrefix.org.intellij.")

        // kotlinx.coroutines
        val kotlinCoroutinesEscapedVersion = kotlinCoroutinesVersion.filter { it != '.' }
        relocate("kotlinx.coroutines.", "$shadePrefix.kotlinx.coroutines${kotlinCoroutinesEscapedVersion}.")
        relocate("_COROUTINE.", "${shadePrefix}._COROUTINE.")
        relocate("reactor.", "$shadePrefix.reactor.")
        relocate("org.reactivestreams.", "$shadePrefix.org.reactivestreams.")

        // JDBC
        relocate("com.zaxxer.hikari.", "$shadePrefix.com.zaxxer.hikari.")
        dependencies {
            exclude(dependency("org.slf4j:.*"))
        }

        relocate("org.postgresql.", "$shadePrefix.org.postgresql.")

        // MongoDB
        relocate("com.mongodb.", "$shadePrefix.com.mongodb.")
        relocate("org.bson.", "$shadePrefix.org.bson.")
    }

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(25)
}
