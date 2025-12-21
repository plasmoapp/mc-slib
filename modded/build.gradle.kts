import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
}

val minecraftVersion = stonecutter.current.project.substringBefore('-')
val platform = stonecutter.current.project.substringAfter('-')

group = rootProject.group
base.archivesName.set("slib-$platform-$minecraftVersion")

val isFabric = stonecutter.constants.getOrDefault("fabric", false)
val isForge = stonecutter.constants.getOrDefault("forge", false)
val isNeoForge = stonecutter.constants.getOrDefault("neoforge", false)

val javaVersion = when {
    stonecutter.eval(minecraftVersion, ">=1.20.5") -> 21
    stonecutter.eval(minecraftVersion, ">=1.18") -> 17
    stonecutter.eval(minecraftVersion, ">=1.17") -> 16
    else -> 8
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
kotlin.jvmToolchain(javaVersion)
tasks.runServer {
    dependsOn(tasks.compileTestJava)
    dependsOn(tasks.compileTestKotlin)
    dependsOn(tasks.processTestResources)

    javaLauncher = project.javaToolchains.launcherFor {
        languageVersion = java.toolchain.languageVersion
    }
}

// use gradle tasks
loom.runs.forEach { it.ideConfigGenerated(false) }

val shadowBundle: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

if (isFabric) {
    tasks.runServer {
        classpath += sourceSets.test.get().output
    }
}

loom {
    mods {
        findByName("main")?.apply {
            sourceSet(sourceSets.test.get())
            mainResourceDirectory.set(sourceSets.test.get().output.resourcesDir)
        }
    }
}

if (isForge && stonecutter.eval(minecraftVersion, "<1.20.2")) {
    loom.forge {
        mixinConfig("slib-forge.mixins.json")
    }
}

configurations {
    loomDevelopmentDependencies.extendsFrom(implementation)
}

dependencies {
    minecraft("net.minecraft:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())

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
        loomDevelopmentDependencies(it)
        shadowBundle(it) { isTransitive = false }
    }

    testCompileOnly(testFixtures(project(":common-server")))
    loomDevelopmentDependencies(testFixtures(project(":common-server")))

    if (isFabric) {
        modImplementation("net.fabricmc:fabric-loader:0.17.3")

        val fabricApiVersion = property("deps.fabric_api") as String

        fun fabricApiModules(vararg module: String) {
            module.forEach {
                modImplementation(fabricApi.module("fabric-$it", fabricApiVersion))
            }
        }

        fabricApiModules("networking-api-v1", "lifecycle-events-v1")
        if (stonecutter.eval(minecraftVersion, ">=1.19")) {
            fabricApiModules("command-api-v2")
        } else {
            fabricApiModules("command-api-v1")
        }

        libs.fabric.permissions.also {
            modImplementation(it) { isTransitive = false }
        }
    } else if (isNeoForge) {
        "neoForge"("net.neoforged:neoforge:${property("deps.neoforge")}")
    } else if (isForge) {
        "forge"("net.minecraftforge:forge:${property("deps.forge")}")
    }
}

fun ProcessResources.expandMatching(
    match: List<String>,
    vararg properties: Pair<String, Any>,
) {
    doFirst {
        filesMatching(match) {
            expand(*properties)
        }
    }
}

tasks {
    processResources {
        expandMatching(
            listOf("fabric.mod.json"),
            "version" to rootProject.version,
        )

        expandMatching(
            listOf("*.mixins.json"),
            "mcVersion" to minecraftVersion,
            "loader" to platform
        )
    }

    runServer {
        doFirst {
            val runDirectory = workingDir.resolve("run")
            runDirectory.mkdirs()

            val eulaFile = runDirectory.resolve("eula.txt")
            if (!eulaFile.exists() || eulaFile.readText().contains("eula=false")) {
                eulaFile.writeText("eula=true")
            }
        }
    }
}

if (isForge && stonecutter.eval(minecraftVersion, ">1.20.3")) {
    // https://github.com/architectury/architectury-loom/issues/191#issuecomment-2030567486
    afterEvaluate {
        tasks.runServer {
            classpath = classpath.filter {
                !it.toString().contains("org.lwjgl") && !it.toString().contains("fabric-log4j-util")
            }
        }
    }

    // https://github.com/architectury/architectury-loom/issues/191#issuecomment-2613841899
    configurations.configureEach {
        resolutionStrategy {
            force("net.sf.jopt-simple:jopt-simple:5.0.4")
        }
    }
}

stonecutter {
    fun fromFile(direction: Boolean, path: String) {
        file(project(":modded").layout.projectDirectory.file(path))
            .readText()
            .lines()
            .filter { it.isNotBlank() }
            .map { it.trim() }
            .filter { !it.startsWith("#") }
            .map { it.substringBefore(" ") to it.substringAfter(" ") }
            .forEach { (replaceFrom, replaceTo) ->
                replacements.string {
                    this.direction = direction
                    replace(replaceFrom, replaceTo)
                }
            }
    }

    fromFile(eval(current.version, "<1.18"), "1.18.2-1.17.1.txt")
    fromFile(eval(current.version, "<1.17"), "1.17.1-1.16.5.txt")
    fromFile(eval(current.version, ">1.20.1"), "1.20.1-1.20.2.txt")
    fromFile(eval(current.version, ">1.21.9"), "1.21.9-1.21.10.txt")
}

tasks {
    shadowJar {
        configurations = listOf(shadowBundle)

        mergeServiceFiles()
        exclude("META-INF/*.kotlin_module")

        // todo: neoforge should use its own mixin without refmap

        if (isForge) {
            exclude("fabric.mod.json")
            exclude("slib-neoforge.mixins.json")

            if (stonecutter.eval(minecraftVersion, ">=1.20.2")) {
                exclude("slib-forge.mixins.json")
            }
        } else if (isNeoForge) {
            exclude("fabric.mod.json")
            exclude("slib-forge.mixins.json")
            exclude("slib.mixins.json")

            rename("slib-neoforge.mixins.json", "slib.mixins.json")
        } else {
            exclude("slib-forge.mixins.json")
            exclude("slib-neoforge.mixins.json")
        }
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
    }
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {
            artifactId = "$minecraftVersion-$platform"
        }
    }
}
