import net.fabricmc.loom.LoomGradlePlugin
import net.fabricmc.loom.LoomNoRemapGradlePlugin
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.fabricapi.FabricApiExtension
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("com.gradleup.shadow")
    alias(libs.plugins.fletchingtable)
}

val minecraftVersion = stonecutter.current.project.substringBefore('-')
val platform = stonecutter.current.project.substringAfter('-')

val noMappings = stonecutter.eval(minecraftVersion, ">=26.1")

if (noMappings) {
    apply<LoomNoRemapGradlePlugin>()

    configurations.api.get().extendsFrom(configurations.create("modApi"))
    configurations.implementation.get().extendsFrom(configurations.create("modImplementation"))
    configurations.compileOnly.get().extendsFrom(configurations.create("modCompileOnly"))
    configurations.runtimeOnly.get().extendsFrom(configurations.create("modRuntimeOnly"))
} else {
    apply<LoomGradlePlugin>()
}

val loom = the<LoomGradleExtensionAPI>()

group = rootProject.group
base.archivesName.set("slib-$platform-$minecraftVersion")

val isFabric = stonecutter.constants.getOrDefault("fabric", false)
val isNeoForge = stonecutter.constants.getOrDefault("neoforge", false)

val javaVersion = if (stonecutter.eval(minecraftVersion, ">=26.1")) 25 else 21

java.toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
kotlin.jvmToolchain(javaVersion)
tasks.named<JavaExec>("runServer") {
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
    tasks.named<JavaExec>("runServer") {
        classpath += sourceSets.test.get().output
    }
}

loom.mods.findByName("main")?.apply {
    sourceSet(sourceSets.test.get())
    mainResourceDirectory.set(sourceSets.test.get().output.resourcesDir)
}

configurations {
    named("loomDevelopmentDependencies") { extendsFrom(configurations.getByName("implementation")) }
}

fletchingTable {
    j52j.register("main") {
        extension("json", "*.json5")
    }
}

dependencies {
    val minecraftVersionDep = (findProperty("deps.minecraft") as? String)
        ?: stonecutter.current.project.substringBefore('-')

    "minecraft"("com.mojang:minecraft:$minecraftVersionDep")
    if (!noMappings) {
        "mappings"(loom.officialMojangMappings())
    }

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
        "loomDevelopmentDependencies"(it)
        shadowBundle(it) { isTransitive = false }
    }

    testCompileOnly(testFixtures(project(":common-server")))
    "loomDevelopmentDependencies"(testFixtures(project(":common-server")))

    if (isFabric) {
        "modImplementation"("net.fabricmc:fabric-loader:0.18.4")

        val fabricApiVersion = property("deps.fabric_api") as String

        fun fabricApiModules(vararg module: String) {
            module.forEach {
                val fabricApi = project.extensions.getByName("fabricApi") as FabricApiExtension
                "modImplementation"(fabricApi.module("fabric-$it", fabricApiVersion))
            }
        }

        fabricApiModules("networking-api-v1", "lifecycle-events-v1", "command-api-v2")

        if (stonecutter.eval(minecraftVersion, ">=26.1")) {
            fabricApiModules("permission-api-v1")

            "modImplementation"("me.lucko:fabric-permissions-api:0.6.3+26.1-SNAPSHOT") {
                isTransitive = false
            }
        } else {
            libs.fabric.permissions.also {
                "modCompileOnly"(it) { isTransitive = false }
            }
        }
        (findProperty("deps.melius_vanish") as? String)?.let { meliusVanishVersion ->
            "modCompileOnly"("maven.modrinth:vanish:$meliusVanishVersion")
        }
    } else if (isNeoForge) {
        "neoForge"("net.neoforged:neoforge:${property("deps.neoforge")}")

        (findProperty("deps.vanishmod_file_id") as? String)?.let { fileId ->
            "modCompileOnly"("curse.maven:vanishmod-423535:$fileId")
        }
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
    }

    named<JavaExec>("runServer") {
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

    fromFile(eval(current.version, ">1.21.9"), "1.21.9-1.21.10.txt")
    fromFile(eval(current.version, ">1.21.11"), "1.21.11-26.1.txt")
}

tasks {
    shadowJar {
        configurations = listOf(shadowBundle)

        mergeServiceFiles()
        exclude("META-INF/*.kotlin_module")

        if (isNeoForge) {
            exclude("fabric.mod.json")
        }
    }

    if (!noMappings) {
        named<RemapJarTask>("remapJar") {
            dependsOn(shadowJar)
            inputFile.set(shadowJar.get().archiveFile)
        }
    } else {
        configurations {
            listOf(apiElements, runtimeElements).forEach {
                it.get().outgoing.artifacts.clear()
                it.get().outgoing.artifact(shadowJar)
            }
        }
    }
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {
            artifactId = "$minecraftVersion-$platform"
        }
    }
}
