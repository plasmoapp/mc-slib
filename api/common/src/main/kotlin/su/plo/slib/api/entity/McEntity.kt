package su.plo.slib.api.entity

import su.plo.slib.api.position.Pos3d
import java.util.*

/**
 * Represents a Minecraft entity
 */
interface McEntity {

    /**
     * Gets entity id
     */
    val id: Int

    /**
     * Gets entity unique id
     */
    val uuid: UUID

    /**
     * Gets entity eye height
     */
    val eyeHeight: Double

    /**
     * Gets entity hit box width
     */
    val hitBoxWidth: Float

    /**
     * Gets entity hit box height
     */
    val hitBoxHeight: Float

    /**
     * Gets position from [getInstance] to local [Pos3d] instance and returns this instance
     */
    fun getPosition(): Pos3d

    /**
     * Gets position from [getInstance] to provided [Pos3d] instance and returns this instance
     *
     * @param position to which the original values will be copied from [getInstance]
     */
    fun getPosition(position: Pos3d): Pos3d

    /**
     * Gets look angle from [getInstance] to local [Pos3d] instance and returns this instance
     */
    fun getLookAngle(): Pos3d

    /**
     * Gets position from [getInstance] to provided [Pos3d] instance and returns this instance
     *
     * @param lookAngle to which the original values will be copied from [getInstance]
     */
    fun getLookAngle(lookAngle: Pos3d): Pos3d

    /**
     * Gets the server's implementation instance
     *
     *  * `org.bukkit.entity.LivingEntity` for bukkit
     *  * `net.minecraft.world.entity.Entity` for mods (fabric/forge mojmap)
     *
     * @return server's implementation object
     */
    fun <T> getInstance(): T
}
