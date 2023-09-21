package su.plo.slib.api.proxy.connection

/**
 * Represents Minecraft connection
 *
 * Player<->Proxy
 *
 * Proxy<->Backend
 */
interface McProxyConnection {

    /**
     * Sends packet to channel
     */
    fun sendPacket(channel: String, data: ByteArray)
}
