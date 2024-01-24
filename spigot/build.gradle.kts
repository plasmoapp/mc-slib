plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    compileOnly(libs.spigot)
    testCompileOnly(libs.spigot)

    api(project(":api:api-common"))
    api(project(":api:api-server"))
    api(project(":common"))
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        archiveAppendix.set("")
        archiveClassifier.set("")
    }

    build {
        dependsOn.add(shadowJar)
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
