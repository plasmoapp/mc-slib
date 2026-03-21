package su.plo.slib

import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.jar.Attributes
import java.util.jar.Manifest

plugins {
    java
    id("com.gradleup.shadow")
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        mergeServiceFiles()

        archiveAppendix.set("")
        archiveClassifier.set("")

        exclude("META-INF/versions/**")

        // Shadow 9.x merges Multi-Release from transitive deps (examination, option),
        // but the actual META-INF/versions classes aren't included, breaking Forge's securejarhandler
        doLast {
            val jarFile = archiveFile.get().asFile
            val uri = URI.create("jar:${jarFile.toURI()}")
            FileSystems.newFileSystem(uri, mapOf("create" to "false")).use { fs ->
                val manifestPath = fs.getPath("META-INF", "MANIFEST.MF")
                val manifest = Manifest()
                Files.newInputStream(manifestPath).use { manifest.read(it) }
                if (manifest.mainAttributes.remove(Attributes.Name("Multi-Release")) != null) {
                    Files.newOutputStream(manifestPath).use { manifest.write(it) }
                }
            }
        }
    }

    build {
        dependsOn.add(shadowJar)
    }
}
