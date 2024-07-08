plugins {
    id("su.plo.slib.shadow-platform")
}

dependencies {
    compileOnly(libs.spigot)
    testCompileOnly(libs.spigot)

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

    build {
        dependsOn(finalJar)
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

afterEvaluate {
    tasks.generateMetadataFileForMavenPublication {
        dependsOn(tasks.getByName("finalJar"))
    }
}