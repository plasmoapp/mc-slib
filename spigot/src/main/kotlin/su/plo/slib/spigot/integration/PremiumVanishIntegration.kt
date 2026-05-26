package su.plo.slib.spigot.integration

import de.myzelyam.api.vanish.PostPlayerHideEvent
import de.myzelyam.api.vanish.PostPlayerShowEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import su.plo.slib.api.event.player.McPlayerVisibilityChangedEvent
import su.plo.slib.spigot.SpigotServerLib

class PremiumVanishIntegration(
    private val minecraftServer: SpigotServerLib,
) : Listener {
    @EventHandler
    fun onPlayerHide(event: PostPlayerHideEvent) {
        val target = minecraftServer.getPlayerByInstance(event.player)
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, true)
    }

    @EventHandler
    fun onPlayerShow(event: PostPlayerShowEvent) {
        val target = minecraftServer.getPlayerByInstance(event.player)
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, false)
    }
}
