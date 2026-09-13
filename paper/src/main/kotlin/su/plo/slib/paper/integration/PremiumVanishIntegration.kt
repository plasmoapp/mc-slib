package su.plo.slib.paper.integration

import de.myzelyam.api.vanish.PostPlayerHideEvent
import de.myzelyam.api.vanish.PostPlayerShowEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import su.plo.slib.api.event.player.McPlayerVisibilityChangedEvent
import su.plo.slib.paper.PaperServerLib

class PremiumVanishIntegration(
    private val minecraftServer: PaperServerLib,
) : Listener {
    @EventHandler
    fun onPlayerHide(event: PostPlayerHideEvent) {
        val target = minecraftServer.getPlayerById(event.player.uniqueId) ?: return
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, true)
    }

    @EventHandler
    fun onPlayerShow(event: PostPlayerShowEvent) {
        val target = minecraftServer.getPlayerById(event.player.uniqueId) ?: return
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, false)
    }
}
