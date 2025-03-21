plugins {
    id("su.plo.slib.shadow-platform")
}

dependencies {
    compileOnly(libs.minestom)

    compileOnly(project(":common"))
    listOf(
        project(":api:api-common"),
        project(":api:api-server"),
        project(":common-integration"),
        project(":common", "shadow")
    ).forEach {
        api(it)
        shadow(it) { isTransitive = false }
    }
}

tasks {
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "21"
        }
    }
}
