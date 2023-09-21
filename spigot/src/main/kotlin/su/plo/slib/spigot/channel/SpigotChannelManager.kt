package su.plo.slib.spigot.channel

import com.google.common.collect.ListMultimap
import com.google.common.collect.Multimaps
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.channel.McChannelHandler
import su.plo.slib.api.server.channel.McChannelManager
import java.util.HashMap
import java.util.LinkedList

class SpigotChannelManager(
    private val loader: JavaPlugin,
    private val minecraftServer: McServerLib
) : McChannelManager, PluginMessageListener {

    private val internalHandlers: ListMultimap<String, McChannelHandler> =
        Multimaps.newListMultimap(HashMap(), ::LinkedList)

    override fun registerChannelHandler(channel: String, handler: McChannelHandler) {
        if (internalHandlers.containsKey(channel)) {
            internalHandlers.put(channel, handler)
            return
        }

        loader.server.messenger.registerIncomingPluginChannel(loader, channel, this)
        loader.server.messenger.registerOutgoingPluginChannel(loader, channel)
    }

    override fun onPluginMessageReceived(channelName: String, player: Player, message: ByteArray) {
        val handlers = internalHandlers[channelName] ?: return

        handlers.forEach {
            it.receive(minecraftServer.getPlayerByInstance(player), message)
        }
    }
}
