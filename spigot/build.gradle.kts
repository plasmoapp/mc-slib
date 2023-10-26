plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    compileOnly(libs.folia)
    compileOnly(libs.spigot)
    testCompileOnly(libs.folia)

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

    shadow(libs.crowdin) {
        isTransitive = false
    }
    shadow(libs.toml4j) {
        isTransitive = false
    }
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        archiveAppendix.set("")
        archiveClassifier.set("")

        relocate("su.plo.crowdin", "su.plo.slib.libs.crowdin")
        relocate("com.moandjiezana.toml", "su.plo.slib.libs.toml")
    }

    build {
        dependsOn.add(shadowJar)
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
