plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    compileOnly(libs.velocity)
    testCompileOnly(libs.velocity)

    api(project(":api:api-common"))
    api(project(":api:api-proxy"))
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
        toolchain.languageVersion.set(JavaLanguageVersion.of(11))
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}
