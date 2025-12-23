import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.register

plugins {
    id("su.plo.slib.shadow-platform")
    alias(libs.plugins.run.waterfall)
}

val testShadowBundle: Configuration by configurations.creating

repositories {
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.bungee.api)
    testCompileOnly(libs.bungee.api)
    compileOnly(libs.bungee.proxy)

    testCompileOnly(testFixtures(project(":common-proxy")))
    testShadowBundle(testFixtures(project(":common-proxy")))

    compileOnly(project(":common"))
    compileOnly(project(":common-integration"))
    listOf(
        project(":api:api-common"),
        project(":api:api-proxy"),
        project(":common", "shadow")
    ).forEach {
        api(it)
        shadow(it) { isTransitive = false }
    }

    compileOnly(libs.adventure.bungee)
    shadow(libs.adventure.bungee) {
        exclude("org.jetbrains", "annotations")
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-text-serializer-gson")
        exclude("net.kyori", "adventure-text-serializer-legacy")
        exclude("net.kyori", "adventure-text-minimessage")
        exclude("net.kyori", "examination-api")
        exclude("net.kyori", "examination-string")
    }
}

runWaterfallExtension {
    disablePluginJarDetection()
}

tasks {
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

    runWaterfall {
        waterfallVersion("1.21")
        pluginJars.from(testJar)
    }

    build {
        dependsOn(finalJar)
    }
}

afterEvaluate {
    tasks.generateMetadataFileForMavenPublication {
        dependsOn(tasks.getByName("finalJar"))
    }
}
