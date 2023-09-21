package su.plo.slib.mod.channel

import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.server.event.player.PlayerRegisterChannelsEvent
import su.plo.slib.mod.extension.toMcServerPlayer

//#if FABRIC
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerGamePacketListenerImpl
//#endif

object RegisterChannelHandler
    //#if FABRIC
    : S2CPlayChannelEvents.Register
    //#endif
{
    //#if FABRIC
    override fun onChannelRegister(
        handler: ServerGamePacketListenerImpl,
        sender: PacketSender,
        server: MinecraftServer,
        channels: MutableList<ResourceLocation>
    ) {
        firePlayerRegisterChannels(handler.player, channels.map { it.toString() })
    }
    //#endif

    fun firePlayerRegisterChannels(player: ServerPlayer, channels: List<String>) {
        player.toMcServerPlayer()?.let {
            PlayerRegisterChannelsEvent.invoker.onPlayerRegisterChannels(it, channels)
        }
    }
}
