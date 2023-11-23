package su.plo.slib.api.proxy.event.player

import su.plo.slib.api.event.GlobalEvent
import su.plo.slib.api.proxy.event.player.McProxyServerConnectedEvent.Callback
import su.plo.slib.api.proxy.player.McProxyPlayer
import su.plo.slib.api.proxy.server.McProxyServerInfo

/**
 * An event fired after the player was successfully connected to the server.
 */
object McProxyServerConnectedEvent
    : GlobalEvent<Callback>(
    { callbacks ->
        Callback { player, previousServer ->
            callbacks.forEach { callback -> callback.onServerConnected(player, previousServer) }
        }
    }
) {
    fun interface Callback {

        fun onServerConnected(player: McProxyPlayer, previousServer: McProxyServerInfo?)
    }
}
