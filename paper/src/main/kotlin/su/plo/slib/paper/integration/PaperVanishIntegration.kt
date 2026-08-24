package su.plo.slib.paper.integration

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerHideEntityEvent
import org.bukkit.event.player.PlayerShowEntityEvent
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.event.player.McPlayerVisibilityChangedEvent
import su.plo.slib.paper.PaperServerLib
import su.plo.slib.paper.util.SchedulerUtil
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PaperVanishIntegration(
    private val minecraftServer: PaperServerLib,
    private val plugin: JavaPlugin,
) : Listener {
    private val pendingHide: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val pendingShow: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    @EventHandler(ignoreCancelled = true)
    fun onPlayerHideEntity(event: PlayerHideEntityEvent) {
        val hiddenPlayer = event.entity as? Player ?: return
        if (!pendingHide.add(hiddenPlayer.uniqueId)) return

        SchedulerUtil.runTaskFor(
            hiddenPlayer,
            plugin,
            {
                pendingHide.remove(hiddenPlayer.uniqueId)

                val target = minecraftServer.getPlayerById(hiddenPlayer.uniqueId) ?: return@runTaskFor

                McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, true)
            },
            { pendingHide.remove(hiddenPlayer.uniqueId) }
        )
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerShowEntity(event: PlayerShowEntityEvent) {
        val shownPlayer = event.entity as? Player ?: return
        if (!pendingShow.add(shownPlayer.uniqueId)) return

        SchedulerUtil.runTaskFor(
            shownPlayer,
            plugin,
            {
                pendingShow.remove(shownPlayer.uniqueId)

                val target = minecraftServer.getPlayerById(shownPlayer.uniqueId) ?: return@runTaskFor

                McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, false)
            },
            { pendingShow.remove(shownPlayer.uniqueId) },
        )
    }
}
