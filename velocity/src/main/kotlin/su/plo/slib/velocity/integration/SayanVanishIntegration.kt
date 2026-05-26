package su.plo.slib.velocity.integration

import com.velocitypowered.api.event.Subscribe
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.velocity.event.VelocityUserUnVanishEvent
import org.sayandev.sayanvanish.velocity.event.VelocityUserVanishEvent
import su.plo.slib.api.event.player.McPlayerVisibilityChangedEvent
import su.plo.slib.api.event.player.McPlayerVisibilityCheckEvent
import su.plo.slib.velocity.VelocityProxyLib

class SayanVanishIntegration(
    private val minecraftProxy: VelocityProxyLib,
) {
    private val api by lazy { SayanVanishAPI.getInstance() }

    init {
        McPlayerVisibilityCheckEvent.registerListener { viewer, target ->
            val targetUser = api.getUser(target.uuid) ?: return@registerListener false
            val viewerUser = api.getUser(viewer.uuid)

            !api.canSee(viewerUser, targetUser)
        }
    }

    @Subscribe
    fun onPlayerVanish(event: VelocityUserVanishEvent) {
        val target = minecraftProxy.getPlayerById(event.user.uniqueId) ?: return
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, true)
    }

    @Subscribe
    fun onPlayerUnVanish(event: VelocityUserUnVanishEvent) {
        val target = minecraftProxy.getPlayerById(event.user.uniqueId) ?: return
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, false)
    }
}
