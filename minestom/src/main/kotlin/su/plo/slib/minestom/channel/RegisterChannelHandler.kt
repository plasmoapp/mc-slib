package su.plo.slib.minestom.channel

import kotlinx.coroutines.*
import net.minestom.server.event.player.PlayerPluginMessageEvent
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent
import su.plo.slib.minestom.MinestomServerLib
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

class RegisterChannelHandler(
    private val minecraftServer: MinestomServerLib
) {

    private val channelsUpdates: MutableMap<UUID, MutableList<String>> = ConcurrentHashMap()
    private val channelsFutures: MutableMap<UUID, Job> = ConcurrentHashMap()

    val pluginChannelRegisterFaker: Consumer<PlayerPluginMessageEvent> = Consumer { event ->
        if (event.identifier != "minecraft:register") return@Consumer

        val player = event.player
        val channelList = event.messageString.split("\u0000")

        for (channel in channelList) {
            val updates = channelsUpdates.computeIfAbsent(
                    player.uuid
            ) { _ -> ArrayList() }
            if (updates.contains(channel)) return@Consumer
            updates.add(channel)

            channelsFutures[player.uuid]?.cancel()
            channelsFutures[player.uuid] = CoroutineScope(Dispatchers.Default).launch {
                delay(500L)

                channelsFutures.remove(player.uuid)

                val channels = channelsUpdates.remove(player.uuid) ?: return@launch

                val mcServerPlayer = minecraftServer.getPlayerByInstance(player)
                McPlayerRegisterChannelsEvent.invoker.onPlayerRegisterChannels(mcServerPlayer, channels)
            }
        }
    }
}
