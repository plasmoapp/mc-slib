package su.plo.slib.api.proxy.server

import java.net.SocketAddress

/**
 * Represents information about a backend Minecraft server.
 */
interface McProxyServerInfo {

    /**
     * Gets the name of the backend server.
     */
    val name: String

    /**
     * Gets the network address of the backend server.
     */
    val address: SocketAddress

    /**
     * Gets the current player count on the backend server.
     */
    val playerCount: Int
}
