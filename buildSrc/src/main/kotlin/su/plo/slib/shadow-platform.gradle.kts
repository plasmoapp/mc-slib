package su.plo.slib

plugins {
    java
    id("com.gradleup.shadow")
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        mergeServiceFiles()

        archiveAppendix.set("")
        archiveClassifier.set("")
    }

    build {
        dependsOn.add(shadowJar)
    }
}
