import org.jetbrains.dokka.gradle.DokkaExtension

plugins {
    id("org.jetbrains.dokka")
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")

    configure<DokkaExtension> {
        modulePath.set(project.name)
    }
}

dependencies {
    subprojects.forEach { dokka(it) }
}
