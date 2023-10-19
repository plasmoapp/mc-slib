package su.plo.slib.api.entity

import su.plo.slib.api.position.Pos3d
import java.util.*

/**
 * Represents a Minecraft entity.
 */
interface McEntity {

    /**
     * Gets the unique identifier of the entity.
     */
    val uuid: UUID

    /**
     * Gets the entity's numerical identifier.
     */
    val id: Int

    /**
     * Gets the height of the entity's eyes from its base.
     */
    val eyeHeight: Double

    /**
     * Gets the width of the entity's hitbox.
     */
    val hitBoxWidth: Float

    /**
     * Gets the height of the entity's hitbox.
     */
    val hitBoxHeight: Float

    /**
     * Retrieves the position of the entity.
     *
     * @return A [Pos3d] instance representing the entity's position.
     */
    fun getPosition(): Pos3d

    /**
     * Retrieves the entity's position and copies it to the provided [position] instance.
     *
     * @param position The [Pos3d] instance to copy the position values to.
     * @return The provided [Pos3d] instance with updated position values.
     */
    fun getPosition(position: Pos3d): Pos3d

    /**
     * Retrieves the entity's look angle.
     *
     * @return A [Pos3d] instance representing the entity's look angle.
     */
    fun getLookAngle(): Pos3d

    /**
     * Retrieves the entity's look angle and copies it to the provided [lookAngle] instance.
     *
     * @param lookAngle The [Pos3d] instance to copy the look angle values to.
     * @return The provided [Pos3d] instance with updated look angle values.
     */
    fun getLookAngle(lookAngle: Pos3d): Pos3d

    /**
     * Gets the server's implementation instance for this entity.
     *
     * The return type may vary depending on the server platform:
     *   - For Bukkit: [org.bukkit.entity.LivingEntity]
     *   - For modded servers (Fabric/Forge): [net.minecraft.world.entity.Entity]
     *
     * @return The server's implementation object associated with this entity.
     * @param T The expected type of the server's implementation instance.
     */
    fun <T> getInstance(): T
}
