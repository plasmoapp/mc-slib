package su.plo.slib.mod.integration

//? if fabric && >=1.18.2 {
import me.drex.vanish.api.VanishAPI
import me.drex.vanish.api.VanishEvents
import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.event.player.McPlayerVisibilityChangedEvent
import su.plo.slib.api.event.player.McPlayerVisibilityCheckEvent
import su.plo.slib.mod.ModServerLib

object MeliusVanishIntegration {
    fun register() {
        McPlayerVisibilityCheckEvent.registerListener { viewer, target ->
            val viewerPlayer = viewer.getInstance<ServerPlayer>()
            val targetPlayer = target.getInstance<ServerPlayer>()

            !VanishAPI.canSeePlayer(targetPlayer, viewerPlayer)
        }

        VanishEvents.VANISH_EVENT.register { player, vanished ->
            val target = ModServerLib.getPlayerByInstance(player)

            McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, vanished)
        }
    }
}
//?}
