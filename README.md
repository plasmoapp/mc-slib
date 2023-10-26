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

Initialization: [FabricMod.kt](//todo)

### Forge
```kotlin
implementation("su.plo.slib:forge-$mcVersion:$libVersion")
```

Initialization: [ForgeMod.kt](//todo)

### Spigot
```kotlin
implementation("su.plo.slib:spigot:$libVersion")
```

Initialization: [SpigotPlugin.kt](//todo)

### BungeeCord
```kotlin
implementation("su.plo.slib:bungee:$libVersion")
```

Initialization: [BungeePlugin.kt](//todo)

### Velocity
```kotlin
implementation("su.plo.slib:velocity:$libVersion")
```

Initialization: [VelocityPlugin.kt](//todo)


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
            <td>1.18.2+</td>
        </tr>
    </tbody>
</table>

For instance, with minecraft 1.20, you need to use 1.19.3:
```kotlin
implementation("su.plo.slib:$platform-1.19.3:$libVersion")
````
