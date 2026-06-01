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
val isForge = stonecutter.constants.getOrDefault("forge", false)
val isNeoForge = stonecutter.constants.getOrDefault("neoforge", false)

val javaVersion = when {
    stonecutter.eval(minecraftVersion, ">=26.1") -> 25
    stonecutter.eval(minecraftVersion, ">=1.20.5") -> 21
    stonecutter.eval(minecraftVersion, ">=1.18") -> 17
    stonecutter.eval(minecraftVersion, ">=1.17") -> 16
    else -> 8
}

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

if (isForge) {
    loom.forge {
        mixinConfig("slib.mixins.json")
    }
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

        fabricApiModules("networking-api-v1", "lifecycle-events-v1")
        if (stonecutter.eval(minecraftVersion, ">=1.19")) {
            fabricApiModules("command-api-v2")
        } else {
            fabricApiModules("command-api-v1")
        }

        if (stonecutter.eval(minecraftVersion, ">=26.1")) {
            "modImplementation"("me.lucko:fabric-permissions-api:0.6.3+26.1-SNAPSHOT") {
                isTransitive = false
            }
        } else {
            libs.fabric.permissions.also {
                "modImplementation"(it) { isTransitive = false }
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
    } else if (isForge) {
        "forge"("net.minecraftforge:forge:${property("deps.forge")}")

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

if (isForge && stonecutter.eval(minecraftVersion, ">1.20.3")) {
    // https://github.com/architectury/architectury-loom/issues/191#issuecomment-2030567486
    afterEvaluate {
        tasks.named<JavaExec>("runServer") {
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
    fromFile(eval(current.version, ">1.21.11"), "1.21.11-26.1.txt")
}

tasks {
    shadowJar {
        configurations = listOf(shadowBundle)

        mergeServiceFiles()
        exclude("META-INF/*.kotlin_module")

        if (isForge) {
            exclude("fabric.mod.json")
        } else if (isNeoForge) {
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
