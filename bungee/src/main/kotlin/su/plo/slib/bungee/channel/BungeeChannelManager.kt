package su.plo.slib.bungee.channel

import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
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

class BungeeChannelManager(
    private val proxyServer: ProxyServer,
    private val minecraftProxy: McProxyLib
) : McProxyChannelManager, Listener {

    private val internalHandlers: SetMultimap<String, McProxyChannelHandler> =
        Multimaps.newSetMultimap(HashMap(), ::HashSet)

    private val registeredReceivers: MutableSet<String> = java.util.HashSet()

    override fun registerChannelHandler(channel: String, handler: McProxyChannelHandler) {
        if (internalHandlers.containsKey(channel) || registeredReceivers.contains(channel)) {
            internalHandlers.put(channel, handler)
            return
        } else {
            registeredReceivers.add(channel)
            internalHandlers.put(channel, handler)
        }

        proxyServer.registerChannel(channel)
    }

    override fun unregisterChannelHandler(channel: String, handler: McProxyChannelHandler) {
        internalHandlers.remove(channel, handler)
    }

    override fun clear() {
        internalHandlers.clear()
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
