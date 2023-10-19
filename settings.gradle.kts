import org.gradle.kotlin.dsl.support.listFilesOrdered

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        google()

        maven("https://jitpack.io/")
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://repo.plo.su")
    }

    plugins {
        val egtVersion = "0.2.2"
        id("gg.essential.defaults") version egtVersion
        id("gg.essential.multi-version.root") version egtVersion
    }
}

rootProject.name = "PlasmoCrossPlatformLib"

// API
file("api").listFilesOrdered {
    return@listFilesOrdered it.isDirectory && it.name != "build"
}.forEach {
    include("api:${it.name}")
    project(":api:${it.name}").apply {
        // rename to solve conflict with "common" project
        // for some reason, gradle thinks that ":common" and ":api:common" is the same,
        // and when you're trying to use ":api:common" as dependency is "common" project,
        // gradle marks it as "Circular dependency"
        name = "api-${it.name}"
    }
}

// Implementations
include("common")
include("spigot")
include("velocity")
include("bungee")

// Modded
include("versions")
project(":versions").apply {
    projectDir = file("versions/")
    buildFileName = "root.gradle.kts"
}

file("versions").listFilesOrdered {
    return@listFilesOrdered it.isDirectory && it.name.contains("-")
}.forEach {
    include("versions:${it.name}")
    project(":versions:${it.name}").apply {
        projectDir = file("versions/${it.name}")
        buildFileName = "../build.gradle.kts"
    }
}
