plugins {
    id("su.plo.slib.shadow-platform")
}

dependencies {
    api(project(":api:api-common"))

    listOf(
        libs.adventure.api,
        libs.adventure.gson,
        libs.adventure.legacy,
        libs.adventure.minimessage
    ).forEach {
        compileOnly(it)
        testImplementation(it)

        shadow(it) {
            exclude("org.jetbrains", "annotations")
            exclude("com.google.code.gson", "gson")
        }
    }

    compileOnly(rootProject.libs.slf4j)
}

tasks {
    shadowJar {
        archiveClassifier = "all"

        exclude("classpath.index")
        relocate("net.kyori", "su.plo.slib.libs.adventure")
    }
}