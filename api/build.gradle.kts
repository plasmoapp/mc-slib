@file:OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)

import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationExtension

plugins {
    id("org.jetbrains.dokka")
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")

    configure<DokkaExtension> {
        modulePath.set(project.name)
    }

    (extensions.getByName("kotlin") as ExtensionAware)
        .extensions
        .configure<AbiValidationExtension>("abiValidation") {
            enabled.set(true)
        }

    tasks.named("check") {
        dependsOn(tasks.named("checkLegacyAbi"))
    }
}

dependencies {
    subprojects.forEach { dokka(it) }
}
