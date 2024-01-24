plugins {
    id("com.github.johnrengelman.shadow")
}

repositories {
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly(libs.bungee.api)
    testCompileOnly(libs.bungee.api)
    compileOnly(libs.bungee.proxy)

    api(project(":api:api-common"))
    api(project(":api:api-proxy"))
    api(project(":common"))
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
