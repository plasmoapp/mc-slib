plugins {
    id("su.plo.slib.shadow-platform")
}

dependencies {
    compileOnly(libs.minestom)
    compileOnly(libs.minestom.extension)
    testCompileOnly(libs.minestom)
    testCompileOnly(libs.minestom.extension)

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
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
