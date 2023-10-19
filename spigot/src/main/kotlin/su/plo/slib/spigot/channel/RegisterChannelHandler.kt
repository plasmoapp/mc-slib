package su.plo.slib.spigot.channel

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRegisterChannelEvent
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent
import su.plo.slib.spigot.SpigotServerLib
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class RegisterChannelHandler(
    private val minecraftServer: SpigotServerLib
) : Listener {

    private val channelsUpdates: MutableMap<UUID, MutableList<String>> = ConcurrentHashMap()
    private val channelsFutures: MutableMap<UUID, ScheduledFuture<*>> = ConcurrentHashMap()

    @EventHandler
    fun onPlayerRegisterChannel(event: PlayerRegisterChannelEvent) {
        val player = event.player
        val channel = event.channel

        val updates = channelsUpdates.computeIfAbsent(
            player.uniqueId
        ) { _ -> ArrayList() }
        if (updates.contains(channel)) return
        updates.add(channel)

        channelsFutures[player.uniqueId]?.cancel(false)
        channelsFutures[player.uniqueId] = minecraftServer.backgroundExecutor.schedule({
            channelsFutures.remove(player.uniqueId)

            val channels = channelsUpdates.remove(player.uniqueId) ?: return@schedule

            val mcServerPlayer = minecraftServer.getPlayerByInstance(player)
            McPlayerRegisterChannelsEvent.invoker.onPlayerRegisterChannels(mcServerPlayer, channels)
        }, 500L, TimeUnit.MILLISECONDS)
    }
}
