import gg.essential.gradle.multiversion.excludeKotlinDefaultImpls

plugins {
    kotlin("jvm")
    id("gg.essential.multi-version")
    id("gg.essential.defaults")
    id("com.github.johnrengelman.shadow")
}

base.archivesName.set("slib-${platform.loaderStr}-${platform.mcVersionStr}")

val shadowCommon by configurations.creating

dependencies {
    compileOnly(project(":common"))
    testCompileOnly(project(":common"))
    listOf(
        project(":api:api-common"),
        project(":api:api-server"),
        project(":common-integration"),
        project(":common", "shadow")
    ).forEach {
        compileOnly(it)
        testCompileOnly(it)
        shadowCommon(it) { isTransitive = false }
    }

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
            12100 -> "0.100.4+1.21"
            12102 -> "0.105.3+1.21.2"
            12105 -> "0.119.1+1.21.5"
            else -> throw GradleException("Unsupported platform $platform")
        }

        fun fabricApiModules(vararg module: String) {
            module.forEach {
                modImplementation(fabricApi.module("fabric-$it", fabricApiVersion))
            }
        }

        fabricApiModules("networking-api-v1", "lifecycle-events-v1")
        if (platform.mcVersion >= 11900) {
            fabricApiModules("command-api-v2")
        } else {
            fabricApiModules("command-api-v1")
        }

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
        excludeKotlinDefaultImpls()
    }

    shadowJar {
        configurations = listOf(shadowCommon)

        mergeServiceFiles()
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

