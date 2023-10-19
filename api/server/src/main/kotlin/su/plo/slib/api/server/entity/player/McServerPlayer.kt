package su.plo.slib.api.server.entity.player

import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.entity.player.McPlayer

/**
 * Represents a player in a Minecraft server.
 */
interface McServerPlayer : McPlayer, McServerEntity {

    /**
     * Checks if the player is currently in spectator mode.
     */
    val isSpectator: Boolean

    /**
     * Checks if the player is sneaking.
     */
    val isSneaking: Boolean

    /**
     * Checks if the player has a scoreboard display below their name.
     */
    val hasLabelScoreboard: Boolean

    /**
     * Gets a collection of registered mod channels for the player.
     */
    val registeredChannels: Collection<String>

    /**
     * Gets the entity that the player is following when in spectator mode.
     *
     * @return The followed entity, or null if the player is not in spectator mode.
     */
    val spectatorTarget: McServerEntity?

    /**
     * Determines if this player can see another [player].
     *
     * @param player The player to check visibility for.
     * @return true if this player can see the specified player; otherwise, false.
     */
    fun canSee(player: McServerPlayer): Boolean

    /**
     * Gets the server's implementation instance for this player.
     *
     * The return type may vary depending on the server platform:
     *   - For Bukkit: [org.bukkit.entity.Player]
     *   - For modded servers (Fabric/Forge): [net.minecraft.server.level.ServerPlayer]
     *
     * @return The server's implementation object associated with this entity.
     * @param T The expected type of the entity object.
     */
    override fun <T> getInstance(): T
}
