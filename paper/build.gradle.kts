import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.semver4j.Semver

plugins {
    id("su.plo.slib.shadow-platform")
    alias(libs.plugins.run.paper)
}

val testShadowBundle: Configuration by configurations.creating

dependencies {
    compileOnly(libs.paper)
    testCompileOnly(libs.paper)

    // pin 1.3.10 with ArgumentType#parse(reader, source)
    // 1.21.1 actually runs on 1.3.10, so it's compile-only quirk
    compileOnly(libs.brigadier.paper)

    compileOnly(libs.semver4j)
    shadow(libs.semver4j)

    testCompileOnly(testFixtures(project(":common-server")))
    testShadowBundle(testFixtures(project(":common-server")))

    compileOnly(project(":common", "shadow"))
    compileOnly(project(":common-integration"))
    listOf(
        project(":api:api-common"),
        project(":api:api-server"),
        project(":common", "shadow")
    ).forEach {
        api(it)
        shadow(it) { isTransitive = false }
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

tasks {
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    shadowJar {
        archiveClassifier = "all"

        relocate("org.jspecify", "su.plo.slib.libs.jspecify")
        relocate("org.semver4j", "su.plo.slib.libs.semver4j")
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

    val datapackSource = project(":common-server")
        .layout.projectDirectory.dir("src/testFixtures/datapack").asFile

    runServer {
        doFirst {
            val runDirectory = runDirectory.get().asFile
            runDirectory.mkdirs()

            val eulaFile = runDirectory.resolve("eula.txt")
            if (!eulaFile.exists() || eulaFile.readText().contains("eula=false")) {
                eulaFile.writeText("eula=true")
            }

            val datapack = runDirectory.resolve("world/datapacks/slib-test")
            datapack.deleteRecursively()
            datapackSource.copyRecursively(datapack)
        }

        val mcVersion = project.property("paper.run_minecraft_version") as String
        val mcSemVersion = Semver.coerce(mcVersion)
            ?: throw RuntimeException("Failed to parse minecraft version: $mcVersion")

        val javaVersion = when {
            mcSemVersion.satisfies(">=26.1") -> 25
            else -> 21
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
