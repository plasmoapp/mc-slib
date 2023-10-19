package su.plo.slib.velocity.connection

import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.connection.McProxyServerConnection
import su.plo.slib.api.proxy.server.McProxyServerInfo

class VelocityProxyServerConnection(
    private val minecraftProxy: McProxyLib,
    val instance: ServerConnection
) : McProxyServerConnection {

    override val serverInfo: McProxyServerInfo
        get() = minecraftProxy.getServerInfoByServerInstance(instance.server)

    override fun sendPacket(channel: String, data: ByteArray) {
        instance.sendPluginMessage(MinecraftChannelIdentifier.from(channel), data)
    }
}
