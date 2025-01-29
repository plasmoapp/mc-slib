package su.plo.slib.api.server.entity

import su.plo.slib.api.entity.McEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.api.server.world.McServerWorld

/**
 * Represents an entity in a Minecraft server.
 */
interface McServerEntity : McEntity {

    /**
     * Gets the world in which the entity is currently located.
     */
    val world: McServerWorld

    /**
     * Checks if entity is not removed from the world.
     */
    fun isValid(): Boolean

    /**
     * Creates a new instance of [ServerPos3d] representing the entity's position.
     */
    fun getServerPosition(): ServerPos3d

    /**
     * Copies all position information to the provided [ServerPos3d] instance without creating a new instance of [ServerPos3d].
     *
     * @param position The [ServerPos3d] instance to copy the position to.
     * @return The same [ServerPos3d] instance with updated position information.
     */
    fun getServerPosition(position: ServerPos3d): ServerPos3d
}
