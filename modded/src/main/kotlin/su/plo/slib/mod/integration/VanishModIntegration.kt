package su.plo.slib.mod.integration

//? if (forge || neoforge) && >=1.18.2 {

/*import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import redstonedubstep.mods.vanishmod.VanishUtil
import redstonedubstep.mods.vanishmod.api.PlayerVanishEvent
import su.plo.slib.api.event.player.McPlayerVisibilityChangedEvent
import su.plo.slib.api.event.player.McPlayerVisibilityCheckEvent
import su.plo.slib.mod.ModServerLib

//? if neoforge {
/^import net.neoforged.neoforge.common.NeoForge
import net.neoforged.bus.api.SubscribeEvent
^///?} else {
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
//?}

object VanishModIntegration {
    fun register() {
        McPlayerVisibilityCheckEvent.registerListener { viewer, target ->
            VanishUtil.isVanished(target.getInstance<Player>(), viewer.getInstance<Player>())
        }

        //? if neoforge {
        /^NeoForge.EVENT_BUS.register(this)
        ^///?} else {
        MinecraftForge.EVENT_BUS.register(this)
        //?}
    }

    @SubscribeEvent
    fun onPlayerVanish(event: PlayerVanishEvent) {
        val player = event.entity as? ServerPlayer ?: return
        val target = ModServerLib.getPlayerByInstance(player)

        McPlayerVisibilityChangedEvent.invoker.onVisibilityChanged(target, event.isVanished)
    }
}
*///?}
