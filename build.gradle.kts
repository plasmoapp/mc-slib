import gg.essential.gradle.util.setJvmDefault
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

// Version
val targetJavaVersion: String by rootProject

plugins {
//    java
    idea
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.idea.ext)

    kotlin("jvm") version("1.6.10")

    id("gg.essential.multi-version.root") apply(false)
}

subprojects {
    if (project.buildFile.name.equals("root.gradle.kts")) return@subprojects

    apply(plugin = "idea")
    apply(plugin = "maven-publish")
//    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "kotlin")

    dependencies {
        api(kotlin("stdlib-jdk8"))
        api(rootProject.libs.kotlinx.coroutines)
        api(rootProject.libs.kotlinx.coroutines.jdk8)
        api(rootProject.libs.kotlinx.json)

        compileOnly(rootProject.libs.guava)
        compileOnly(rootProject.libs.gson)
        compileOnly(rootProject.libs.guice)

        api(rootProject.libs.slf4j)
    }

    tasks {
        java {
            withSourcesJar()

            toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))

            val os: OperatingSystem = DefaultNativePlatform.getCurrentOperatingSystem()
            if (os.isMacOsX) {
                toolchain.vendor.set(JvmVendorSpec.AZUL)
            }
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

    val publishDisabled = setOf("api")
    if (!publishDisabled.contains(project.name)) {
        publishing {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
//                    artifactId = "${platform.mcVersionStr}-${platform.loaderStr}"
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
