package su.plo.slib.bungee.integration

import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.bungeecord.event.BungeeUserUnVanishEvent
import org.sayandev.sayanvanish.bungeecord.event.BungeeUserVanishEvent
import su.plo.slib.api.event.player.McPlayerVisibilityChangedEvent
import su.plo.slib.api.event.player.McPlayerVisibilityCheckEvent
import su.plo.slib.bungee.BungeeProxyLib

class SayanVanishIntegration(
    private val minecraftProxy: BungeeProxyLib,
) : Listener {
    private val api by lazy { SayanVanishAPI.getInstance() }

    init {
        McPlayerVisibilityCheckEvent.registerListener { viewer, target ->
            val targetUser = api.getUser(target.uuid) ?: return@registerListener false
            val viewerUser = api.getUser(viewer.uuid)

            !api.canSee(viewerUser, targetUser)
        }
    }

    @EventHandler
    fun onPlayerVanish(event: BungeeUserVanishEvent) {
        val target = minecraftProxy.getPlayerById(event.user.uniqueId) ?: return
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, true)
    }

    @EventHandler
    fun onPlayerUnVanish(event: BungeeUserUnVanishEvent) {
        val target = minecraftProxy.getPlayerById(event.user.uniqueId) ?: return
        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, false)
    }
}
