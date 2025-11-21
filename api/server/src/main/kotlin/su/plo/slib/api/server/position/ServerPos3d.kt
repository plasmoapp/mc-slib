package su.plo.slib.api.server.position

import su.plo.slib.api.position.Pos3d
import su.plo.slib.api.server.world.McServerWorld
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import kotlin.math.cos
import kotlin.math.sin

/**
 * Represents a mutable position in the world, similar to a "Location" in Bukkit.
 *
 * This class defines a 3D position within the Minecraft world, including coordinates (x, y, z) and orientation
 * (yaw and pitch).
 *
 * @property world The [McServerWorld] in which this position is located.
 * @property x The x-coordinate of the position.
 * @property y The y-coordinate of the position.
 * @property z The z-coordinate of the position.
 * @property yaw The yaw (horizontal rotation) angle of the entity at this position (default is 0 degrees).
 * @property pitch The pitch (vertical rotation) angle of the entity at this position (default is 0 degrees).
 */
class ServerPos3d @JvmOverloads constructor(
    world: McServerWorld?,
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float = 0f,
    var pitch: Float = 0f
) {

    constructor() : this(null, 0.0, 0.0, 0.0)

    private var worldReference: Reference<McServerWorld> = WeakReference(world)

    var world: McServerWorld
        get() = worldReference.get()!!
        set(value) {
            if (worldReference.get() != value) {
                worldReference = WeakReference(value)
            }
        }

    /**
     * Calculates the squared distance between this position and another position.
     *
     * @param o The target position to calculate the distance to.
     * @return The squared distance between the two positions.
     * @throws IllegalArgumentException if the positions are in different worlds.
     */
    fun distanceSquared(o: ServerPos3d): Double {
        require(o.world == world) { "Cannot measure distance between worlds" }

        val xDiff = x - o.x
        val yDiff = y - o.y
        val zDiff = z - o.z
        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff
    }

    /**
     * Converts this server position to a simple 3D position (x, y, z) represented by [Pos3d].
     *
     * @return A [Pos3d] object representing the same 3D position.
     */
    fun toPosition(): Pos3d {
        return Pos3d(x, y, z)
    }

    /**
     * Computes the unit vector (look angle) representing the entity's orientation based on its yaw and pitch angles.
     *
     * @return A [Pos3d] unit vector representing the look angle.
     */
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
