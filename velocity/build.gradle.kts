plugins {
    id("su.plo.slib.shadow-platform")
}

dependencies {
    compileOnly(libs.velocity)
    testCompileOnly(libs.velocity)

    compileOnly(project(":common"))
    listOf(
        project(":api:api-common"),
        project(":api:api-proxy"),
        project(":common-integration"),
        project(":common", "shadow")
    ).forEach {
        api(it)
        shadow(it) { isTransitive = false }
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

tasks {
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(11))
    }
}
