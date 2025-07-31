package su.plo.slib.spigot.extension

import org.bukkit.Location
import su.plo.slib.api.server.position.ServerPos3d

fun ServerPos3d.toLocation(): Location =
    Location(
        world.getInstance(),
        x,
        y,
        z,
        yaw,
        pitch,
    )
