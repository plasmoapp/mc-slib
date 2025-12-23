plugins {
    idea
    `maven-publish`

    kotlin("jvm") version(libs.versions.kotlin.get())
    alias(libs.plugins.dokka)

    alias(libs.plugins.architectury) apply false
}

subprojects {
    if (project.path in listOf(":modded")) return@subprojects

    apply(plugin = "idea")
    apply(plugin = "maven-publish")
    apply(plugin = "kotlin")

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(rootProject.libs.kotlinx.coroutines)
        implementation(rootProject.libs.kotlinx.coroutines.jdk8)

        implementation(rootProject.libs.guava)
        api("com.mojang:brigadier:1.0.18")
    }

    tasks {
        java {
            withSourcesJar()

            toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        }

        compileJava {
            options.encoding = Charsets.UTF_8.name()
        }

        javadoc {
            options.encoding = Charsets.UTF_8.name()
        }

        processResources {
            filteringCharset = Charsets.UTF_8.name()
        }

        kotlin {
            compilerOptions {
                val key = "-Xjvm-default="
                freeCompilerArgs.set(freeCompilerArgs.get().filterNot { it.startsWith(key) } + listOf(key + "all"))
            }
        }
    }

    if (project.name != "api") {
        publishing {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                }
            }

            repositories {
                maven("https://repo.plasmoverse.com/prs") {
                    name = "plasmoversePrs"

                    credentials {
                        username = System.getenv("MAVEN_USERNAME")
                        password = System.getenv("MAVEN_PASSWORD")
                    }
                }

                maven("https://repo.plasmoverse.com/releases") {
                    name = "plasmoverseReleases"

                    credentials {
                        username = System.getenv("MAVEN_USERNAME")
                        password = System.getenv("MAVEN_PASSWORD")
                    }
                }
            }
        }
    }
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://oss.sonatype.org/content/repositories/snapshots")

        maven("https://repo.papermc.io/repository/maven-public/")

        maven("https://repo.plasmoverse.com/snapshots")
        maven("https://repo.plo.su")

        maven("https://jitpack.io/")
    }
}

tasks {
    build {
        doLast {
            jar.get().archiveFile.get().asFile.delete()
        }
    }
}
