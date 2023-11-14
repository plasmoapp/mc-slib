import gg.essential.gradle.multiversion.excludeKotlinDefaultImpls
import gg.essential.gradle.multiversion.mergePlatformSpecifics
import kotlinx.validation.api.publishedApiAnnotationName

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
    if (platform.isFabric) {
        val fabricApiVersion = when (platform.mcVersion) {
            11605 -> "0.42.0+1.16"
            11701 -> "0.46.1+1.17"
            11802 -> "0.76.0+1.18.2"
            11903 -> "0.73.2+1.19.3"
            else -> throw GradleException("Unsupported platform $platform")
        }

        modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
        libs.fabric.permissions.also {
            modImplementation(it)
        }
    }

    val includedProjects = listOf(
        ":api:api-common",
        ":api:api-server",
        ":common"
    )

    includedProjects.forEach {
        api(project(it))
        shadowCommon(project(it)) {
            isTransitive = false
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

