package su.plo.slib.api.position

/**
 * Represents a mutable vec3
 */
data class Pos3d(
    var x: Double,
    var y: Double,
    var z: Double
) {
    constructor() : this(0.0, 0.0, 0.0)

    fun distanceSquared(o: Pos3d): Double {
        val xDiff = x - o.x
        val yDiff = y - o.y
        val zDiff = z - o.z

        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff
    }
}
