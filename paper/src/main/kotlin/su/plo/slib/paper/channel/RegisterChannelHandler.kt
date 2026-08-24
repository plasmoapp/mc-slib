package su.plo.slib.paper.channel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRegisterChannelEvent
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent
import su.plo.slib.paper.PaperServerLib
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class RegisterChannelHandler(
    private val minecraftServer: PaperServerLib
) : Listener {

    private val channelsUpdates: MutableMap<UUID, MutableList<String>> = ConcurrentHashMap()
    private val channelsFutures: MutableMap<UUID, Job> = ConcurrentHashMap()

    @EventHandler
    fun onPlayerRegisterChannel(event: PlayerRegisterChannelEvent) {
        val playerId = event.player.uniqueId
        val channel = event.channel

        val updates = channelsUpdates.computeIfAbsent(playerId) { _ -> ArrayList() }
        if (updates.contains(channel)) return
        updates.add(channel)

        channelsFutures[playerId]?.cancel()
        channelsFutures[playerId] = CoroutineScope(Dispatchers.Default).launch {
            delay(500L)

            channelsFutures.remove(playerId)

            val channels = channelsUpdates.remove(playerId) ?: return@launch

            val mcServerPlayer = minecraftServer.getPlayerById(playerId) ?: return@launch

            McPlayerRegisterChannelsEvent.invoker.onPlayerRegisterChannels(mcServerPlayer, channels)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val playerId = event.player.uniqueId

        channelsFutures.remove(playerId)?.cancel()
        channelsUpdates.remove(playerId)
    }
}
