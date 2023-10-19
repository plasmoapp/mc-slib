package su.plo.slib.api.proxy.channel

import su.plo.slib.api.proxy.connection.McProxyConnection

/**
 * Represents a Minecraft proxy channel handler.
 *
 * Channel handlers can be registered using the [McProxyChannelHandler].
 */
interface McProxyChannelHandler {

    /**
     * Invoked when a message is received on the associated channel.
     **
     * @param source The [McProxyConnection] representing the connection sending the message.
     * @param destination The [McProxyConnection] representing the connection receiving the message.
     * @param data The byte array containing the received message data.
     * @return `true` if a message should be marked as handled, `false` otherwise.
     * Handled messages will not be forwarded to the destination.
     */
    fun receive(source: McProxyConnection, destination: McProxyConnection, data: ByteArray): Boolean
}
