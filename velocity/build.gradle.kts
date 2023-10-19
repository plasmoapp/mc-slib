plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    compileOnly(libs.velocity)

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
