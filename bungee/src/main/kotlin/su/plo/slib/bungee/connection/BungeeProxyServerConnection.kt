package su.plo.slib.bungee.connection

import net.md_5.bungee.api.connection.Server
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.connection.McProxyServerConnection
import su.plo.slib.api.proxy.server.McProxyServerInfo

class BungeeProxyServerConnection(
    private val minecraftProxy: McProxyLib,
    val instance: Server
) : McProxyServerConnection {

    override val serverInfo: McProxyServerInfo
        get() = minecraftProxy.getServerInfoByServerInstance(instance.info)

    override fun sendPacket(channel: String, data: ByteArray) =
        instance.sendData(channel, data)
}
