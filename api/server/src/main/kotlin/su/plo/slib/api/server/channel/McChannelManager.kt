package su.plo.slib.api.server.channel

interface McChannelManager {

    /**
     * Registers a channel [handler] on the given [channel]
     */
    fun registerChannelHandler(channel: String, handler: McChannelHandler)
}
