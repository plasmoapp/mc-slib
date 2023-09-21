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
implementation("su.plo.crosslib:api-common:$libVersion")
```

### Server
API for server platforms: Spigot/Fabric/Forge
```kotlin
implementation("su.plo.crosslib:api-server:$libVersion")
```

#### Proxy
API for proxy platforms: Velocity/BungeeCord
```kotlin
implementation("su.plo.crosslib:api-proxy:$libVersion")
```

## Platforms

### Fabric
```kotlin
modImplementation(include("su.plo.crosslib:fabric-$mcVersion:$libVersion")!!)
```

### Forge
```kotlin
implementation("su.plo.crosslib:forge-$mcVersion:$libVersion")
```

### Spigot
```kotlin
implementation("su.plo.crosslib:spigot:$libVersion")
```

### BungeeCord
```kotlin
implementation("su.plo.crosslib:bungee:$libVersion")
```

### Velocity
```kotlin
implementation("su.plo.crosslib:velocity:$libVersion")
```


## Version

### Fabric/Forge minecraft versions
There is not all minecraft versions for Fabric/Forge platforms, so you need to use older/newer supported minecraft version
<table>
    <tbody>
        <tr>
            <th>Lib minecraft version</th>
            <th>Supported minecraft versions</th>
        </tr>
        <tr>
            <td>1.16.5</td>
            <td>1.16.5</td>
        </tr>
        <tr>
            <td>1.17.1</td>
            <td>[1.17.1 - 1.18.2]</td>
        </tr>
        <tr>
            <td>1.19.3</td>
            <td>1.19.2+</td>
        </tr>
    </tbody>
</table>

For instanc, with minecraft 1.20 you need to use 1.19.3:
```kotlin
implementation("su.plo.crosslib:$platform-1.19.3:$libVersion")
````
