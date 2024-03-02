plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    compileOnly(libs.minestom)
    compileOnly(libs.minestom.extension)
    testCompileOnly(libs.minestom)
    testCompileOnly(libs.minestom.extension)

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
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
