package su.plo.slib.velocity.integration

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.proxy.Player
import de.myzelyam.api.vanish.VelocityPlayerHideEvent
import de.myzelyam.api.vanish.VelocityPlayerShowEvent
import de.myzelyam.api.vanish.VelocityVanishAPI
import su.plo.slib.api.event.player.McPlayerVisibilityChangedEvent
import su.plo.slib.api.event.player.McPlayerVisibilityCheckEvent
import su.plo.slib.velocity.VelocityProxyLib

class PremiumVanishIntegration(
    private val minecraftProxy: VelocityProxyLib,
) {
    init {
        McPlayerVisibilityCheckEvent.registerListener { viewer, target ->
            val viewerPlayer = viewer.getInstance<Player>()
            val targetPlayer = target.getInstance<Player>()

            !VelocityVanishAPI.canSee(viewerPlayer, targetPlayer)
        }
    }

    @Subscribe
    fun onPlayerHide(event: VelocityPlayerHideEvent) {
        val target = minecraftProxy.getPlayerByInstance(event.player)
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, true)
    }

    @Subscribe
    fun onPlayerShow(event: VelocityPlayerShowEvent) {
        val target = minecraftProxy.getPlayerByInstance(event.player)
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, false)
    }
}
