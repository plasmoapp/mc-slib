package su.plo.slib.api.proxy.server

import java.net.SocketAddress

/**
 * Represents backend server info
 */
interface McProxyServerInfo {

    val name: String

    val address: SocketAddress

    val playerCount: Int
}
