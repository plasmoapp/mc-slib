plugins {
    id("com.github.johnrengelman.shadow")
}

repositories {
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly(libs.bungee.api)
    compileOnly(libs.bungee.proxy)

    val includedProjects = listOf(
        ":api:api-common",
        ":api:api-proxy",
        ":common"
    )

    includedProjects.forEach {
        api(project(it))
        shadow(project(it)) {
            isTransitive = false
        }
    }
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
}
