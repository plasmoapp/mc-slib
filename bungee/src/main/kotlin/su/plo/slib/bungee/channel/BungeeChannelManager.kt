package su.plo.slib.bungee.channel

import com.google.common.collect.ListMultimap
import com.google.common.collect.Multimaps
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.connection.Server
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.channel.McProxyChannelHandler
import su.plo.slib.api.proxy.channel.McProxyChannelManager
import su.plo.slib.bungee.connection.BungeeProxyServerConnection
import java.util.*

class BungeeChannelManager(
    private val proxyServer: ProxyServer,
    private val minecraftProxy: McProxyLib
) : McProxyChannelManager, Listener {

    private val internalHandlers: ListMultimap<String, McProxyChannelHandler> =
        Multimaps.newListMultimap(HashMap(), ::LinkedList)

    override fun registerChannelHandler(channel: String, handler: McProxyChannelHandler) {
        if (internalHandlers.containsKey(channel)) {
            internalHandlers.put(channel, handler)
            return
        } else {
            internalHandlers.put(channel, handler)
        }

        proxyServer.registerChannel(channel)
    }

    @EventHandler
    fun onPluginMessage(event: PluginMessageEvent) {
        if (event.isCancelled) return

        val handlers = internalHandlers[event.tag] ?: return

        val source = event.sender.toProxyConnection()
        val destination = event.receiver.toProxyConnection()
        val data = event.data

        for (handler in handlers) {
            val handled = handler.receive(source, destination, data)
            if (handled) {
                event.isCancelled = true
                break
            }
        }
    }

    private fun Connection.toProxyConnection() =
        when (this) {
            is Server -> BungeeProxyServerConnection(minecraftProxy, this)

            is ProxiedPlayer -> minecraftProxy.getPlayerByInstance(this)

            else -> throw UnsupportedOperationException("${this::class.java} is not supported")
        }
}
