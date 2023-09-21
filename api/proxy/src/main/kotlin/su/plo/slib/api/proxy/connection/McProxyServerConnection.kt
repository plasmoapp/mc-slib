package su.plo.slib.api.proxy.connection

import su.plo.slib.api.proxy.server.McProxyServerInfo

interface McProxyServerConnection : McProxyConnection {

    /**
     * Gets connection server info
     */
    val serverInfo: McProxyServerInfo
}
