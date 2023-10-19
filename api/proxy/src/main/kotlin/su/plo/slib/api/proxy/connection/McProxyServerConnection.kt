package su.plo.slib.api.proxy.connection

import su.plo.slib.api.proxy.server.McProxyServerInfo

/**
 * Represents a Minecraft connection from the proxy to the backend server.
 */
interface McProxyServerConnection : McProxyConnection {

    /**
     * Gets information about the connection's target backend server.
     */
    val serverInfo: McProxyServerInfo
}
