package su.plo.slib.api.entity.player

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import java.util.*

/**
 * Represents a Minecraft player.
 *
 * This interface is intended for use in the common module.
 *
 * For proxy, consider using [su.plo.slib.api.proxy.player.McProxyPlayer].
 *
 * For server, consider using [su.plo.slib.api.server.entity.player.McServerPlayer].
 */
interface McPlayer : McCommandSource {

    /**
     * Determines whether the player is currently online.
     *
     * @return `true` if the player is online, `false` otherwise.
     */
    val isOnline: Boolean

    /**
     * Retrieves a new instance of [McGameProfile].
     *
     * @return A [McGameProfile] instance representing the player's profile.
     */
    val gameProfile: McGameProfile

    /**
     * Retrieves the unique identifier of the player.
     *
     * @return The UUID associated with the player.
     */
    val uuid: UUID

    /**
     * Retrieves the name of the player.
     *
     * @return The name of the player.
     */
    val name: String

    /**
     * Sends a custom packet to the player to the specified [channel].
     *
     * @param channel The channel to which the packet should be sent.
     * @param data    The byte array containing packet data.
     */
    fun sendPacket(channel: String, data: ByteArray)

    /**
     * Kicks the player from the server with the specified [reason].
     *
     * @param reason A [McTextComponent] representing the reason for kicking the player.
     */
    fun kick(reason: McTextComponent)

    /**
     * Gets the server's implementation instance for this player.
     *
     * @return The server's implementation object associated with this entity.
     * @param T The expected type of the entity object.
     */
    fun <T> getInstance(): T
}
