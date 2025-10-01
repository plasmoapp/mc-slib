import gg.essential.gradle.util.setJvmDefault

plugins {
    idea
    `maven-publish`

    kotlin("jvm") version(libs.versions.kotlin.get())
    alias(libs.plugins.dokka)

    id("gg.essential.multi-version.root") apply(false)
}

subprojects {
    if (project.buildFile.name.equals("root.gradle.kts")) return@subprojects

    apply(plugin = "idea")
    apply(plugin = "maven-publish")
    apply(plugin = "kotlin")

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(rootProject.libs.kotlinx.coroutines)
        implementation(rootProject.libs.kotlinx.coroutines.jdk8)

        implementation(rootProject.libs.guava)
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

        compileKotlin {
            setJvmDefault("all")
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

        maven("https://repo.plasmoverse.com/snapshots")
        maven("https://repo.plo.su")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io/")
        maven("https://maven.neoforged.net/releases")
    }
}

tasks {
    build {
        doLast {
            jar.get().archiveFile.get().asFile.delete()
        }
    }
}
