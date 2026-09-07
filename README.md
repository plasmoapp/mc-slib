# slib 
Universal Minecraft server/proxy library for [Plasmo Voice](https://github.com/plasmoapp/plasmo-voice).

## Maven repository
```kotlin
repositories {
    maven("https://repo.plasmoverse.com/snapshots")
}
```

## API

### Common
Shared API for server and proxy 
```kotlin
implementation("su.plo.slib:api-common:$libVersion")
```

### Server
API for server platforms: Paper/Fabric/NeoForge
```kotlin
implementation("su.plo.slib:api-server:$libVersion")
```

#### Proxy
API for proxy platforms: Velocity/BungeeCord
```kotlin
implementation("su.plo.slib:api-proxy:$libVersion")
```

## Platforms

### Fabric
build.gradle.kts
```kotlin
modImplementation("su.plo.slib:fabric-$mcVersion:$libVersion")
```

You also need to add

Initialization: [TestFabricMod.kt](https://github.com/plasmoapp/mc-slib/blob/main/modded/src/test/kotlin/su/plo/slib/mod/TestFabricMod.kt)

### NeoForge
```kotlin
implementation("su.plo.slib:neoforge-$mcVersion:$libVersion")
```

Initialization: [TestNeoForgeMod.kt](https://github.com/plasmoapp/mc-slib/blob/main/modded/src/test/kotlin/su/plo/slib/mod/TestNeoForgeMod.kt)

### Paper
```kotlin
implementation("su.plo.slib:paper:$libVersion")
```

Initialization: [PaperPlugin.kt](https://github.com/plasmoapp/mc-slib/blob/main/paper/src/test/kotlin/PaperPlugin.kt)

### Minestom
```kotlin
implementation("su.plo.slib:minestom:$libVersion")
```

Initialization: [MinestomExtension.kt](https://github.com/plasmoapp/mc-slib/blob/main/minestom/src/test/kotlin/MinestomExtension.kt)

### BungeeCord
```kotlin
implementation("su.plo.slib:bungee:$libVersion")
```

Initialization: [BungeePlugin.kt](https://github.com/plasmoapp/mc-slib/blob/main/bungee/src/test/kotlin/BungeePlugin.kt)

### Velocity
```kotlin
implementation("su.plo.slib:velocity:$libVersion")
```

Initialization: [VelocityPlugin.kt](https://github.com/plasmoapp/mc-slib/blob/main/velocity/src/test/kotlin/VelocityPlugin.kt)


## Version
<img alt="version" src="https://img.shields.io/badge/dynamic/xml?label=%20&query=/metadata/versioning/versions/version[not(contains(text(),'%2B'))][last()]&url=https://repo.plasmoverse.com/releases/su/plo/slib/common/maven-metadata.xml">

### Fabric/NeoForge minecraft versions
Not every minecraft version is built for the Fabric/NeoForge platforms,
so you need to use the closest older supported version.

The supported versions are listed in [settings.gradle.kts](https://github.com/plasmoapp/mc-slib/blob/main/settings.gradle.kts).

For instance, with minecraft 1.21.1, you need to use 1.21:
```kotlin
implementation("su.plo.slib:$platform-1.21:$libVersion")
````
