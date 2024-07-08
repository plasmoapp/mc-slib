plugins {
    id("su.plo.slib.shadow-platform")
}

repositories {
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly(libs.bungee.api)
    testCompileOnly(libs.bungee.api)
    compileOnly(libs.bungee.proxy)

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

    build {
        dependsOn(finalJar)
    }
}

afterEvaluate {
    tasks.generateMetadataFileForMavenPublication {
        dependsOn(tasks.getByName("finalJar"))
    }
}