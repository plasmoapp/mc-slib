package su.plo.slib.spigot.util.extension

import org.bukkit.entity.Entity
import su.plo.slib.api.position.Pos3d
import su.plo.slib.api.server.position.ServerPos3d

private val coordinateApiSupported by lazy {
    try {
        Entity::class.java.getMethod("getX")
        Entity::class.java.getMethod("getYaw")
        true
    } catch (_: NoSuchMethodException) {
        false
    }
}

fun Entity.readPosition(position: Pos3d): Pos3d {
    if (coordinateApiSupported) {
        position.x = x
        position.y = y
        position.z = z

        return position
    }

    val entityLocation = location

    position.x = entityLocation.x
    position.y = entityLocation.y
    position.z = entityLocation.z

    return position
}

fun Entity.readPosition(position: ServerPos3d): ServerPos3d {
    if (coordinateApiSupported) {
        position.x = x
        position.y = y
        position.z = z
        position.yaw = yaw
        position.pitch = pitch

        return position
    }

    val entityLocation = location

    position.x = entityLocation.x
    position.y = entityLocation.y
    position.z = entityLocation.z
    position.yaw = entityLocation.yaw
    position.pitch = entityLocation.pitch

    return position
}
