package su.plo.slib.api.proxy.connection

/**
 * Represents a Minecraft connection.
 *
 * Player<->Proxy
 *
 * Proxy<->Backend
 */
interface McProxyConnection {

    /**
     * Sends a packet to the specified channel.
     *
     * @param channel The channel to which the packet should be sent.
     * @param data    The byte array containing packet data.
     */
    fun sendPacket(channel: String, data: ByteArray)
}
