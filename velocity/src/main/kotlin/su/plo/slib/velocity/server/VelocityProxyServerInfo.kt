package su.plo.slib.velocity.server

import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import su.plo.slib.api.proxy.server.McProxyServerInfo
import java.net.SocketAddress

class VelocityProxyServerInfo(
    private val registeredServer: RegisteredServer,
    val instance: ServerInfo = registeredServer.serverInfo
) : McProxyServerInfo {

    override val name: String
        get() = instance.name

    override val address: SocketAddress
        get() = instance.address

    override val playerCount: Int
        get() = registeredServer.playersConnected.size

    override fun equals(other: Any?) =
        this === other ||
                other is ServerInfo && instance == other ||
                other is VelocityProxyServerInfo && instance == other.instance

    override fun hashCode() =
        instance.hashCode()
}
