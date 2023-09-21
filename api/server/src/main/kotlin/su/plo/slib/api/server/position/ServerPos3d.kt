package su.plo.slib.api.server.position

import su.plo.slib.api.position.Pos3d
import su.plo.slib.api.server.world.McServerWorld
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents a mutable position in the world (like Location in Bukkit)
 */
class ServerPos3d @JvmOverloads constructor(
    var world: McServerWorld,
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float = 0f,
    var pitch: Float = 0f
) {
    fun distanceSquared(o: ServerPos3d): Double {
        require(o.world == world) { "Cannot measure distance between worlds" }

        val xDiff = x - o.x
        val yDiff = y - o.y
        val zDiff = z - o.z
        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff
    }

    fun toPosition(): Pos3d {
        return Pos3d(x, y, z)
    }

    val lookAngle: Pos3d
        get() {
            val pos = Pos3d()

            val rotX = yaw.toDouble()
            val rotY = pitch.toDouble()

            pos.y = -sin(Math.toRadians(rotY))

            val xz = cos(Math.toRadians(rotY))

            pos.x = -xz * sin(Math.toRadians(rotX))
            pos.z = xz * cos(Math.toRadians(rotX))

            return pos
        }
}
