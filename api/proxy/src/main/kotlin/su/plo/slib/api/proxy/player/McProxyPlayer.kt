package su.plo.slib.api.proxy.player

import su.plo.slib.api.proxy.connection.McProxyConnection
import su.plo.slib.api.proxy.connection.McProxyServerConnection
import su.plo.slib.api.entity.player.McPlayer

interface McProxyPlayer : McPlayer, McProxyConnection {

    /**
     * Gets player current server
     */
    val server: McProxyServerConnection?
}
