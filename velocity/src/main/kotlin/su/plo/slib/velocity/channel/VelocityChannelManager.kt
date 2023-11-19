package su.plo.slib.velocity.channel

import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.messages.ChannelIdentifier
import com.velocitypowered.api.proxy.messages.ChannelMessageSink
import com.velocitypowered.api.proxy.messages.ChannelMessageSource
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.channel.McProxyChannelHandler
import su.plo.slib.api.proxy.channel.McProxyChannelManager
import su.plo.slib.velocity.connection.VelocityProxyServerConnection

class VelocityChannelManager(
    private val proxyServer: ProxyServer,
    private val minecraftProxy: McProxyLib
) : McProxyChannelManager {

    private val internalHandlers: SetMultimap<ChannelIdentifier, McProxyChannelHandler> =
        Multimaps.newSetMultimap(HashMap(), ::HashSet)

    private val registeredReceivers: MutableSet<String> = HashSet()

    override fun registerChannelHandler(channel: String, handler: McProxyChannelHandler) {
        val mcChannel = MinecraftChannelIdentifier.from(channel)

        if (internalHandlers.containsKey(mcChannel) || registeredReceivers.contains(channel)) {
            internalHandlers.put(mcChannel, handler)
            return
        } else {
            registeredReceivers.add(channel)
            internalHandlers.put(mcChannel, handler)
        }

        proxyServer.channelRegistrar.register(mcChannel)
    }

    override fun unregisterChannelHandler(channel: String, handler: McProxyChannelHandler) {
        internalHandlers.remove(channel, handler)
    }

    override fun clear() {
        internalHandlers.clear()
    }

    @Subscribe
    fun onPluginMessage(event: PluginMessageEvent) {
        if (!event.result.isAllowed) return

        val handlers = internalHandlers[event.identifier] ?: return

        val source = event.source.toProxyConnection()
        val destination = event.target.toProxyConnection()
        val data = event.data

        for (handler in handlers) {
            val handled = handler.receive(source, destination, data)
            if (handled) {
                event.result = PluginMessageEvent.ForwardResult.handled()
                break
            }
        }
    }

    private fun ChannelMessageSource.toProxyConnection() =
        when (this) {
            is ServerConnection -> VelocityProxyServerConnection(minecraftProxy, this)

            is Player -> minecraftProxy.getPlayerByInstance(this)

            else -> throw UnsupportedOperationException("${this::class.java} is not supported")
        }

    private fun ChannelMessageSink.toProxyConnection() =
        when (this) {
            is ServerConnection -> VelocityProxyServerConnection(minecraftProxy, this)

            is Player -> minecraftProxy.getPlayerByInstance(this)

            else -> throw UnsupportedOperationException("${this::class.java} is not supported")
        }

}
