package su.plo.slib.bungee.server

import net.md_5.bungee.api.config.ServerInfo
import su.plo.slib.api.proxy.server.McProxyServerInfo
import java.net.SocketAddress

class BungeeProxyServerInfo(
    val instance: ServerInfo
) : McProxyServerInfo {

    override val name: String
        get() = instance.name

    override val address: SocketAddress
        get() = instance.socketAddress

    override val playerCount: Int
        get() = instance.players.filter { it.isConnected }.size

    override fun equals(other: Any?) =
        this === other ||
                other is ServerInfo && instance == other ||
                other is BungeeProxyServerInfo && instance == other.instance

    override fun hashCode() =
        instance.hashCode()
}
