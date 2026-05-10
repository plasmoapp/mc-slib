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

    testImplementation(libs.log4j.core)
    testImplementation(libs.slf4j.log4j12)

    testImplementation(libs.jline.reader)
    testImplementation(libs.jline.terminal)
    testImplementation(libs.terminalconsoleappender)
}

tasks {
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    register<JavaExec>("runServer") {
        group = "application"

        standardInput = System.`in`

        workingDir = layout.projectDirectory.dir("run").asFile

        doFirst {
            workingDir.mkdirs()
        }

        classpath = sourceSets["test"].runtimeClasspath
        mainClass.set("su.plo.slib.minestom.TestMinestomServerKt")
    }
}
