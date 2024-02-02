package su.plo.slib.spigot.channel

import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.channel.McServerChannelHandler
import su.plo.slib.api.server.channel.McServerChannelManager

class SpigotChannelManager(
    private val loader: JavaPlugin,
    private val minecraftServer: McServerLib
) : McServerChannelManager, PluginMessageListener {

    private val internalHandlers: SetMultimap<String, McServerChannelHandler> =
        Multimaps.newSetMultimap(HashMap(), ::HashSet)

    override val registeredChannels: Collection<String>
        get() = loader.server.messenger.getOutgoingChannels(loader)

    override fun registerChannelHandler(channel: String, handler: McServerChannelHandler) {
        if (internalHandlers.containsKey(channel) || loader.server.messenger.getOutgoingChannels(loader).contains(channel)) {
            internalHandlers.put(channel, handler)
            return
        } else {
            internalHandlers.put(channel, handler)
        }

        loader.server.messenger.registerIncomingPluginChannel(loader, channel, this)
        loader.server.messenger.registerOutgoingPluginChannel(loader, channel)
    }

    override fun unregisterChannelHandler(channel: String, handler: McServerChannelHandler) {
        internalHandlers.remove(channel, handler)

        if (!internalHandlers.containsKey(channel) || internalHandlers.get(channel).isEmpty()) {
            loader.server.messenger.unregisterIncomingPluginChannel(loader, channel, this)
            loader.server.messenger.unregisterOutgoingPluginChannel(loader, channel)
        }
    }

    override fun clear() {
        internalHandlers.clear()

        loader.server.messenger.unregisterIncomingPluginChannel(loader)
        loader.server.messenger.unregisterOutgoingPluginChannel(loader)
    }

    override fun onPluginMessageReceived(channelName: String, player: Player, message: ByteArray) {
        val handlers = internalHandlers[channelName] ?: return

        handlers.forEach {
            it.receive(minecraftServer.getPlayerByInstance(player), message)
        }
    }
}
