package su.plo.slib.bungee.integration

import de.myzelyam.api.vanish.BungeePlayerHideEvent
import de.myzelyam.api.vanish.BungeePlayerShowEvent
import de.myzelyam.api.vanish.BungeeVanishAPI
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import su.plo.slib.api.event.player.McPlayerVisibilityChangedEvent
import su.plo.slib.api.event.player.McPlayerVisibilityCheckEvent
import su.plo.slib.bungee.BungeeProxyLib

class PremiumVanishIntegration(
    private val minecraftProxy: BungeeProxyLib,
) : Listener {
    init {
        McPlayerVisibilityCheckEvent.registerListener { viewer, target ->
            val viewerPlayer = viewer.getInstance<ProxiedPlayer>()
            val targetPlayer = target.getInstance<ProxiedPlayer>()

            !BungeeVanishAPI.canSee(viewerPlayer, targetPlayer)
        }
    }

    @EventHandler
    fun onPlayerHide(event: BungeePlayerHideEvent) {
        val target = minecraftProxy.getPlayerByInstance(event.player)
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, true)
    }

    @EventHandler
    fun onPlayerShow(event: BungeePlayerShowEvent) {
        val target = minecraftProxy.getPlayerByInstance(event.player)
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, false)
    }
}
