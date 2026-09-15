plugins {
    kotlin("jvm")
}

group = "io.github.heyhey123"
version = "1.0-SNAPSHOT"

val rocksdbVersion = "10.4.2"

// 1. 通过 -ProcksdbPlatform / -ProcksdbUseUniversal 控制
val rocksdbPlatform: String? = providers
    .gradleProperty("rocksdbPlatform")
    .orNull // 例: linux-x86_64, macosx-aarch64, windows-x86_64

val useUniversal: Boolean = providers
    .gradleProperty("rocksdbUseUniversal")
    .map { it.equals("true", ignoreCase = true) }
    .orElse(false)
    .get()

// 2. 定义不同平台对应的 artifact classifier
fun rocksdbClassifierFor(platform: String): String =
    when (platform.lowercase()) {
        "linux-x86_64" -> "linux64"
        "linux-aarch64" -> "linux-aarch64"
        "macosx-x86_64" -> "osx"
        "macosx-aarch64" -> "osx-aarch64"
        "windows-x86_64" -> "win64"
        else -> error("不支持的 rocksdb 平台: $platform，请通过 -ProcksdbPlatform=... 传入合法值")
    }

// 3. 组装最终要使用的坐标
val rocksdbDependencyNotation: String = when {
    useUniversal -> {
        // 示例: 通用 jar（所有平台），坐标需按你实际使用的发布版本来改
        // 比如: org.rocksdb:rocksdbjni:10.4.2
        "org.rocksdb:rocksdbjni:$rocksdbVersion"
    }

    rocksdbPlatform != null -> {
        val classifier = rocksdbClassifierFor(rocksdbPlatform)
        // 示例: 带 classifier 的平台特定 native 版
        // 比如: org.rocksdb:rocksdbjni:10.4.2:linux64
        "org.rocksdb:rocksdbjni:$rocksdbVersion:$classifier"
    }

    else -> {
        // 默认行为: 使用通用 jar，防止用户没传参数时构建失败
        "org.rocksdb:rocksdbjni:$rocksdbVersion"
    }
}

dependencies {
    compileOnly(project(":core"))
    implementation(rocksdbDependencyNotation)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}
