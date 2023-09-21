package su.plo.slib.api.server.entity

import su.plo.slib.api.entity.McEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.slib.api.server.world.McServerWorld

/**
 * Represents a Minecraft server entity
 */
interface McServerEntity : McEntity {

    /**
     * @return entity's current world
     */
    val world: McServerWorld

    /**
     * @return a new instance of an entity's [ServerPos3d]
     */
    fun getServerPosition(): ServerPos3d

    /**
     * Copies all position info to provided [ServerPos3d] instance without creating a new instance of [ServerPos3d]
     */
    fun getServerPosition(position: ServerPos3d): ServerPos3d
}
