import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.semver4j.Semver

plugins {
    id("su.plo.slib.shadow-platform")
    alias(libs.plugins.runpaper)
}

val testShadowBundle: Configuration by configurations.creating

dependencies {
    compileOnly(libs.spigot)
    testCompileOnly(libs.spigot)

    testCompileOnly(testFixtures(project(":common-server")))
    testShadowBundle(testFixtures(project(":common-server")))

    compileOnly(project(":common"))
    compileOnly(project(":common-integration"))
    listOf(
        project(":api:api-common"),
        project(":api:api-server"),
        project(":common", "shadow")
    ).forEach {
        api(it)
        shadow(it) { isTransitive = false }
    }

    compileOnly(libs.adventure.bukkit)
    shadow(libs.adventure.bukkit) {
        exclude("org.jetbrains", "annotations")
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-text-serializer-gson")
        exclude("net.kyori", "adventure-text-serializer-legacy")
        exclude("net.kyori", "adventure-text-minimessage")
        exclude("net.kyori", "examination-api")
        exclude("net.kyori", "examination-string")
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

tasks {
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    }

    shadowJar {
        archiveClassifier = "all"

        relocate("net.kyori", "su.plo.slib.libs.adventure")
    }

    val finalJar = register<Jar>("finalJar") {
        dependsOn(jar)
        dependsOn(shadowJar)

        archiveClassifier = ""

        from(zipTree(shadowJar.get().archiveFile))
        from(project(":common-integration").sourceSets.main.get().output)
    }

    val testJar =
        register("testJar", ShadowJar::class) {
            configurations = listOf(testShadowBundle)

            archiveClassifier.set("test")

            from(zipTree(finalJar.get().archiveFile))
            from(sourceSets.test.get().output)
        }

    runServer {
        doFirst {
            val runDirectory = runDirectory.get().asFile
            runDirectory.mkdirs()

            val eulaFile = runDirectory.resolve("eula.txt")
            if (!eulaFile.exists() || eulaFile.readText().contains("eula=false")) {
                eulaFile.writeText("eula=true")
            }
        }

        val mcVersion = project.property("spigot.run_minecraft_version") as String
        val mcSemVersion = Semver(mcVersion)

        val javaVersion = when {
            mcSemVersion.satisfies(">=1.20.5") -> 21
            mcSemVersion.satisfies(">=1.18") -> 17
            mcSemVersion.satisfies(">=1.17") -> 16
            else -> 8
        }

        minecraftVersion(mcVersion)
        runDirectory(layout.projectDirectory.asFile.resolve("run/$mcVersion"))
        jvmArgs("-DPaper.IgnoreJavaVersion=true")

        javaLauncher = project.javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(javaVersion)
        }

        pluginJars.from(testJar)
    }

    build {
        dependsOn(finalJar)
    }
}

runPaper {
    disablePluginJarDetection()
}

afterEvaluate {
    tasks.generateMetadataFileForMavenPublication {
        dependsOn(tasks.getByName("finalJar"))
    }
}
