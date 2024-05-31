import gg.essential.gradle.multiversion.excludeKotlinDefaultImpls
import gg.essential.gradle.multiversion.mergePlatformSpecifics

plugins {
    kotlin("jvm")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
    id("com.github.johnrengelman.shadow")
}

base.archivesName.set("slib-${platform.loaderStr}-${platform.mcVersionStr}")

val shadowCommon by configurations.creating

repositories {
    maven("https://repo.essential.gg/repository/maven-public")
}

dependencies {
    api(project(":api:api-common"))
    api(project(":api:api-server"))
    api(project(":common"))

    if (platform.isFabric) {
        val fabricApiVersion = when (platform.mcVersion) {
            11605 -> "0.42.0+1.16"
            11701 -> "0.46.1+1.17"
            11802 -> "0.76.0+1.18.2"
            11902 -> "0.76.1+1.19.2"
            11903 -> "0.73.2+1.19.3"
            12001 -> "0.90.7+1.20.1"
            12002 -> "0.90.7+1.20.2"
            12004 -> "0.95.4+1.20.4"
            12006 -> "0.97.7+1.20.6"
            12100 -> "0.99.0+1.21"
            else -> throw GradleException("Unsupported platform $platform")
        }

        modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
        libs.fabric.permissions.also {
            modImplementation(it)
        }
    }
}

tasks {
    processResources {
        filesMatching(mutableListOf("*.mixins.json")) {
            expand(
                mutableMapOf(
                    "mcVersion" to platform.mcVersionStr,
                    "loader" to platform.loaderStr
                )
            )
        }
    }

    jar {
        mergePlatformSpecifics()

        if (platform.mcVersion >= 11400) {
            excludeKotlinDefaultImpls()
        }
    }

    shadowJar {
        configurations = listOf(shadowCommon)

        exclude("META-INF/*.kotlin_module")

        if (platform.isForge) {
            exclude("fabric.mod.json")

            if (platform.mcVersion >= 12002) {
                exclude("slib-forge.mixins.json")
            }
        } else {
            exclude("slib-forge.mixins.json")
        }
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
    }

    build {
        doLast {
            remapJar.get().archiveFile.get().asFile
                .copyTo(rootProject.buildDir.resolve("libs/${remapJar.get().archiveFile.get().asFile.name}"), true)
        }
    }
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {
            artifactId = "${platform.mcVersionStr}-${platform.loaderStr}"
        }
    }
}

