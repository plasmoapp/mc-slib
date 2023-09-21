package su.plo.slib.api.server.entity.player

import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.entity.player.McPlayer

/**
 * Represents a Minecraft server player entity
 */
interface McServerPlayer : McPlayer, McServerEntity {

    /**
     * Checks if player in spectator mode
     */
    val isSpectator: Boolean

    /**
     * Checks if player is sneaking
     */
    val isSneaking: Boolean

    /**
     * Checks if the player have scoreboard display below name
     */
    val hasLabelScoreboard: Boolean

    /**
     * Gets collection of registered mods channels
     */
    val registeredChannels: Collection<String>

    /**
     * Gets the entity which is followed by the player when in spectator mode
     *
     * @return the followed entity
     */
    val spectatorTarget: McServerEntity?

    /**
     * @return true if the given [player] is not being hidden from this player
     */
    fun canSee(player: McServerPlayer): Boolean

    /**
     * Gets the server's implementation instance
     *
     *  * `org.bukkit.entity.Player` for bukkit
     *  * `net.minecraft.server.level.ServerPlayer` for mods (fabric/forge mojmap)
     *
     * @return server's implementation object
     */
    override fun <T> getInstance(): T
}
