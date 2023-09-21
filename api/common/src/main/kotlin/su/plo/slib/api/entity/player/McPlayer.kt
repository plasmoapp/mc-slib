package su.plo.slib.api.entity.player

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import java.util.*

/**
 * Represents a Minecraft player
 *
 * Use it when you are using ONLY common module
 *
 * For proxy use MinecraftProxyPlayer (you can also cast MinecraftPlayer to MinecraftProxyPlayer without any problem)
 *
 * For server use MinecraftServerPlayer (you can also cast MinecraftPlayer to MinecraftServerPlayer without any problem)
 */
interface McPlayer : McCommandSource {

    /**
     * @return true if player is online
     */
    val isOnline: Boolean

    /**
     * @return creates a new instance of [McGameProfile] using player info
     */
    val gameProfile: McGameProfile

    /**
     * @return player unique id
     */
    val uuid: UUID

    /**
     * @return player name
     */
    val name: String

    /**
     * Sends the packet to the given [channel]
     */
    fun sendPacket(channel: String, data: ByteArray)

    /**
     * Kicks the player with the given [reason]
     */
    fun kick(reason: McTextComponent)

    /**
     * Gets the backed entity object
     */
    fun <T> getInstance(): T
}
