package su.plo.slib.mod.channel

import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent
import su.plo.slib.mod.extension.toMcServerPlayer
import su.plo.slib.mod.entity.ModServerPlayer
import su.plo.slib.mod.ModServerLib

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
        // skip player if he's not placed in the playerlist yet
        if (ModServerLib.minecraftServer.playerList.getPlayer(player.uuid) == null) return

        val mcServerPlayer = player.toMcServerPlayer() as ModServerPlayer

        McPlayerRegisterChannelsEvent.invoker.onPlayerRegisterChannels(mcServerPlayer, channels)

        channels.forEach { mcServerPlayer.addChannel(it) }
    }
}
