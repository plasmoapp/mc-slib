import org.gradle.kotlin.dsl.support.listFilesOrdered

rootProject.name = "slib"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        google()

        maven("https://jitpack.io/")

        maven("https://maven.architectury.dev")

        maven("https://maven.fabricmc.net")
        maven("https://maven.neoforged.net/releases/")

        maven("https://repo.plo.su")
        maven("https://repo.plasmoverse.com/snapshots")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.7.10"
}

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
include("common-integration")
include("common-server")
include("spigot")
include("minestom")
include("velocity")
include("bungee")

// Modded
enum class ConfigureVersions {
    ALL,
    DEVELOPMENT,
    NONE,
}

val configureVersions = providers.gradleProperty("modded.versions_mode").getOrElse("DEVELOPMENT")
    .let { ConfigureVersions.valueOf(it) }

if (configureVersions != ConfigureVersions.NONE) {
    stonecutter {
        centralScript = "build.gradle.kts"

        create("modded") {
            fun mc(mcVersion: String, vararg loaders: String) =
                loaders.forEach { version("$mcVersion-$it", mcVersion) }

            vcsVersion = "1.19.3-fabric"
            mc("1.19.3", "fabric", "forge")

            if (configureVersions == ConfigureVersions.ALL) {
                mc("1.16.5", "fabric", "forge")
                mc("1.17.1", "fabric", "forge")
                mc("1.18.2", "fabric", "forge")
                mc("1.19.2", "fabric", "forge")
                mc("1.20.1", "fabric", "forge")
                mc("1.20.2", "fabric", "forge")
                mc("1.20.4", "fabric", "forge")
                mc("1.20.6", "fabric")
                mc("1.21", "fabric", "forge", "neoforge")
                mc("1.21.2", "fabric", "neoforge")
                mc("1.21.5", "fabric", "neoforge")
                mc("1.21.6", "fabric", "neoforge")
                mc("1.21.7", "neoforge")
                mc("1.21.9", "fabric", "neoforge")
                mc("1.21.11", "fabric", "neoforge")
            } else {
                val developmentVersions = providers.gradleProperty("modded.versions_dev").getOrElse("")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .map { it.split("-") }

                developmentVersions.forEach { version ->
                    mc(version[0], version[1])
                }
            }
        }
    }
}
