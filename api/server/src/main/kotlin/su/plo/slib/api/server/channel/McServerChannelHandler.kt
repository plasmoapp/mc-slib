package su.plo.slib.api.server.channel

import su.plo.slib.api.server.entity.player.McServerPlayer

/**
 * Represents a Minecraft server channel handler.
 *
 * Channel handlers can be registered using the [McServerChannelManager].
 */
interface McServerChannelHandler {

    /**
     * Invoked when a message is received on the associated channel.
     **
     * @param player The [McServerPlayer] representing the player receiving the message.
     * @param data The byte array containing the received message data.
     */
    fun receive(player: McServerPlayer, data: ByteArray)
}
