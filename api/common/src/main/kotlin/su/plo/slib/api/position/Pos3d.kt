package su.plo.slib.api.position

/**
 * Represents a mutable 3D vector (vec3) with double precision for coordinates.
 */
data class Pos3d(
    var x: Double,
    var y: Double,
    var z: Double
) {

    /**
     * Constructs a `Pos3d` with default coordinates (0.0, 0.0, 0.0).
     */
    constructor() : this(0.0, 0.0, 0.0)

    /**
     * Calculates the squared distance between this vector and another `Pos3d`.
     *
     * @param o The other `Pos3d` vector.
     * @return The squared distance between the two vectors.
     */
    fun distanceSquared(o: Pos3d): Double {
        val xDiff = x - o.x
        val yDiff = y - o.y
        val zDiff = z - o.z

        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff
    }
}
