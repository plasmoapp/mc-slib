package su.plo.slib.minestom.channel

import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import net.minestom.server.event.player.PlayerPluginMessageEvent
import net.minestom.server.extensions.Extension
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.server.channel.McServerChannelHandler
import su.plo.slib.api.server.channel.McServerChannelManager
import java.util.function.Consumer

class MinestomChannelManager(
    extension: Extension,
    private val minecraftServer: McServerLib
) : McServerChannelManager {

    private val internalHandlers: SetMultimap<String, McServerChannelHandler> =
        Multimaps.newSetMultimap(HashMap(), ::HashSet)

    override val registeredChannels: MutableCollection<String> = mutableSetOf()

    override fun registerChannelHandler(channel: String, handler: McServerChannelHandler) {
        if (internalHandlers.containsKey(channel) || registeredChannels.contains(channel)) {
            internalHandlers.put(channel, handler)
            return
        } else {
            internalHandlers.put(channel, handler)
        }

        registeredChannels.add(channel)
    }

    override fun unregisterChannelHandler(channel: String, handler: McServerChannelHandler) {
        internalHandlers.remove(channel, handler)

        if (!internalHandlers.containsKey(channel) || internalHandlers.get(channel).isEmpty()) {
            registeredChannels.remove(channel)
        }
    }

    override fun clear() {
        internalHandlers.clear()
        registeredChannels.clear()
    }

    private val pluginChannelHandler: Consumer<PlayerPluginMessageEvent> = Consumer { event ->
        if (event.identifier.lowercase() == "minecraft:register") return@Consumer
        val handlers = internalHandlers[event.identifier] ?: return@Consumer

        handlers.forEach {
            it.receive(minecraftServer.getPlayerByInstance(event.player), event.message)
        }
    }

    init {
        extension.eventNode.addListener(PlayerPluginMessageEvent::class.java, pluginChannelHandler)
    }
}
