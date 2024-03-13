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
API for server platforms: Spigot/Fabric/Forge
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

Initialization: [FabricMod.kt](https://github.com/plasmoapp/mc-slib/blob/main/versions/src/test/kotlin/FabricMod.kt)

### Forge
```kotlin
implementation("su.plo.slib:forge-$mcVersion:$libVersion")
```

Initialization: [ForgeMod.kt](https://github.com/plasmoapp/mc-slib/blob/main/versions/1.19.3-forge/src/test/kotlin/ForgeMod.kt)

### Spigot
```kotlin
implementation("su.plo.slib:spigot:$libVersion")
```

Initialization: [SpigotPlugin.kt](https://github.com/plasmoapp/mc-slib/blob/main/spigot/src/test/kotlin/SpigotPlugin.kt)

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
<img alt="version" src="https://img.shields.io/badge/dynamic/xml?label=%20&query=/metadata/versioning/versions/version[not(contains(text(),'%2B'))][last()]&url=https://repo.plasmoverse.com/snapshots/su/plo/slib/common/maven-metadata.xml">

### Fabric/Forge minecraft versions
There is not all minecraft versions for Fabric/Forge platforms, so you need to use older/newer supported minecraft version
<table>
    <tbody>
        <tr>
            <th>slib minecraft version</th>
            <th>Supported minecraft versions</th>
        </tr>
        <tr>
            <td>1.16.5</td>
            <td>1.16.5</td>
        </tr>
        <tr>
            <td>1.17.1</td>
            <td>1.17.1</td>
        </tr>
        <tr>
            <td>1.19.3</td>
            <td>1.18.2-1.19.4</td>
        </tr>
        <tr>
            <td>1.20.1</td>
            <td>1.20.1</td>
        </tr>
        <tr>
            <td>1.20.2</td>
            <td>1.20.2+</td>
        </tr>
    </tbody>
</table>

For instance, with minecraft 1.18.2, you need to use 1.19.3:
```kotlin
implementation("su.plo.slib:$platform-1.19.3:$libVersion")
````
