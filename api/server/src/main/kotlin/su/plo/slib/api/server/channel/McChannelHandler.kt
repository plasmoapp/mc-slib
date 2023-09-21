package su.plo.slib.api.server.channel

import su.plo.slib.api.server.entity.player.McServerPlayer

/**
 * To register a channel handler use [McChannelManager]
 */
interface McChannelHandler {


    /**
     * Invoked when message is received on a channel
     */
    fun receive(player: McServerPlayer, data: ByteArray)
}
