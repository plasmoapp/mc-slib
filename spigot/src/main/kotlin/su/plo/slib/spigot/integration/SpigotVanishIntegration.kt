package su.plo.slib.spigot.integration

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerHideEntityEvent
import org.bukkit.event.player.PlayerShowEntityEvent
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.event.player.McPlayerVisibilityChangedEvent
import su.plo.slib.spigot.SpigotServerLib
import su.plo.slib.spigot.util.SchedulerUtil
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class SpigotVanishIntegration(
    private val minecraftServer: SpigotServerLib,
    private val plugin: JavaPlugin,
) : Listener {
    private val pendingHide: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val pendingShow: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    @EventHandler(ignoreCancelled = true)
    fun onPlayerHideEntity(event: PlayerHideEntityEvent) {
        val hiddenPlayer = event.entity as? Player ?: return
        if (!pendingHide.add(hiddenPlayer.uniqueId)) return

        SchedulerUtil.runTaskFor(hiddenPlayer, plugin) {
            pendingHide.remove(hiddenPlayer.uniqueId)

            val target = minecraftServer.getPlayerById(hiddenPlayer.uniqueId) ?: return@runTaskFor

            McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, true)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerShowEntity(event: PlayerShowEntityEvent) {
        val shownPlayer = event.entity as? Player ?: return
        if (!pendingShow.add(shownPlayer.uniqueId)) return

        SchedulerUtil.runTaskFor(shownPlayer, plugin) {
            pendingShow.remove(shownPlayer.uniqueId)

            val target = minecraftServer.getPlayerById(shownPlayer.uniqueId) ?: return@runTaskFor

            McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, false)
        }
    }
}
