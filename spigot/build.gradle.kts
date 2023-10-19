plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    compileOnly(libs.spigot)
    compileOnly(libs.folia)

    val includedProjects = listOf(
        ":api:api-common",
        ":api:api-server",
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
