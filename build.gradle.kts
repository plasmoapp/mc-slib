import gg.essential.gradle.util.setJvmDefault

// Version
val targetJavaVersion: String by rootProject

plugins {
    idea
    `maven-publish`
    alias(libs.plugins.shadow)

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

        implementation(rootProject.libs.slf4j)
        implementation(rootProject.libs.guava)
    }

    tasks {
        java {
            withSourcesJar()

            toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
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

    if (!project.name.contains("api")) {
        publishing {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                }
            }

            repositories {
                if (findProperty("plasmoverseSnapshotsUsername") != null) {
                    maven("https://repo.plasmoverse.com/shapshots") {
                        name = "plasmoverseSnapshots"

                        credentials(PasswordCredentials::class)
                        authentication {
                            create<BasicAuthentication>("basic")
                        }
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

        maven("https://repo.plo.su")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.papermc.io/repository/maven-public/")
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
