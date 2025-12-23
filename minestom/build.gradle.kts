plugins {
    id("su.plo.slib.shadow-platform")
}

dependencies {
    compileOnly(libs.minestom)

    compileOnly(project(":common"))
    listOf(
        project(":api:api-common"),
        project(":api:api-server"),
        project(":common-integration"),
        project(":common", "shadow")
    ).forEach {
        api(it)
        testImplementation(it)
        shadow(it) { isTransitive = false }
    }

    testImplementation(libs.minestom)
    testImplementation(testFixtures(project(":common-server")))

    testImplementation("org.apache.logging.log4j:log4j-core:2.25.3")
    testImplementation("org.slf4j:slf4j-log4j12:2.0.17")
}

tasks {
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    register<JavaExec>("runServer") {
        group = "application"

        workingDir = layout.projectDirectory.dir("run").asFile

        doFirst {
            workingDir.mkdirs()
        }

        classpath = sourceSets["test"].runtimeClasspath
        mainClass.set("su.plo.slib.minestom.TestMinestomServerKt")
    }
}
