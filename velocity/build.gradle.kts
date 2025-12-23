import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("su.plo.slib.shadow-platform")
    alias(libs.plugins.run.velocity)
    kotlin("kapt")
}

val testShadowBundle: Configuration by configurations.creating

dependencies {
    compileOnly(libs.velocity)
    testCompileOnly(libs.velocity)

    testCompileOnly(testFixtures(project(":common-proxy")))
    testShadowBundle(testFixtures(project(":common-proxy")))

    kaptTest(libs.velocity)

    compileOnly(project(":common"))
    listOf(
        project(":api:api-common"),
        project(":api:api-proxy"),
        project(":common-integration"),
        project(":common", "shadow")
    ).forEach {
        api(it)
        shadow(it) { isTransitive = false }
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

runVelocityExtension {
    disablePluginJarDetection()
}

tasks {
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    val testJar =
        register("testJar", ShadowJar::class) {
            configurations = listOf(testShadowBundle)

            archiveClassifier.set("test")

            from(zipTree(shadowJar.get().archiveFile))
            from(sourceSets.test.get().output)
        }

    runVelocity {
        velocityVersion("3.4.0-SNAPSHOT")
        pluginJars.from(testJar)
    }
}
