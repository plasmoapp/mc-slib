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

//#if MC>=12002
//$$ import com.google.common.cache.CacheBuilder
//$$ import net.fabricmc.fabric.api.networking.v1.S2CConfigurationChannelEvents
//$$ import net.minecraft.server.network.ServerConfigurationPacketListenerImpl
//$$ import su.plo.slib.api.event.player.McPlayerJoinEvent
//$$ import java.util.*
//$$ import java.util.concurrent.TimeUnit
//#endif

//#else

//#if MC>=12002
//$$ import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent
//$$ import net.minecraftforge.event.network.ChannelRegistrationChangeEvent
//$$ import net.minecraftforge.network.NetworkContext
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent
//$$ import net.minecraft.server.network.ServerGamePacketListenerImpl
//#endif

//#endif

object RegisterChannelHandler
    //#if FABRIC
    : S2CPlayChannelEvents.Register
    //#endif
{
    //#if FABRIC

    //#if MC>=12002
    //$$ val channelsCache = CacheBuilder.newBuilder()
    //$$     .expireAfterWrite(1L, TimeUnit.MINUTES)
    //$$     .build<UUID, List<String>>()
    //$$ init {
    //$$     McPlayerJoinEvent.registerListener {
    //$$         val channels = channelsCache.getIfPresent(it.uuid) ?: return@registerListener
    //$$         channelsCache.invalidate(it.uuid)
    //$$
    //$$         firePlayerRegisterChannels(it.getInstance(), channels)
    //$$     }
    //$$ }
    //#endif

    override fun onChannelRegister(
        handler: ServerGamePacketListenerImpl,
        sender: PacketSender,
        server: MinecraftServer,
        newChannels: MutableList<ResourceLocation>
    ) {
        //#if MC>=12002
        // 1.20.2 forge sends channels only on configuration state,
        // so we need to save these channels and check them on player join
        //$$ val newChannels = newChannels.map { channel -> channel.toString() }
        //$$
        //$$ val player = handler.player
        //$$ val channels = channelsCache.getIfPresent(player.uuid)
        //$$     ?.let { it + newChannels }
        //$$     ?.toSet()
        //$$     ?.toList()
        //$$     ?: newChannels
        //$$ channelsCache.invalidate(player.uuid)
        //$$
        //$$ firePlayerRegisterChannels(player, channels)
        //#else
        firePlayerRegisterChannels(handler.player, newChannels.map { it.toString() })
        //#endif
    }
    //#else
    //#if MC>=12002
    //$$ @SubscribeEvent
    //$$ fun onPlayerJoin(event: PlayerLoggedInEvent) { // forge clients handler
    //$$     val player = event.entity as? ServerPlayer ?: return
    //$$     val context = NetworkContext.get(player.connection.connection)
    //$$
    //$$     val channels = context.remoteChannels
    //$$         .takeIf { it.isNotEmpty() }
    //$$         ?.map { it.toString() }
    //$$         ?: return
    //$$
    //$$     firePlayerRegisterChannels(player, channels)
    //$$ }
    //$$ @SubscribeEvent
    //$$ fun onRegister(event: ChannelRegistrationChangeEvent) { // fabric clients handler
    //$$     val gameListener = event.source.packetListener as? ServerGamePacketListenerImpl ?: return
    //$$     val player = gameListener.player ?: return
    //$$     val channels = event.channels.map { it.toString() }
    //$$
    //$$     firePlayerRegisterChannels(player, channels)
    //$$ }
    //#endif
    //#endif

    fun firePlayerRegisterChannels(player: ServerPlayer, channels: List<String>) {
        // skip player if he's not placed in the playerlist yet
        if (ModServerLib.minecraftServer.playerList.getPlayer(player.uuid) == null && !hasForgeChannel(channels)) return

        val mcServerPlayer = player.toMcServerPlayer() as ModServerPlayer

        // skip if channels are already registered
        val addedChannels = channels.mapNotNull {
            if (mcServerPlayer.addChannel(it)) {
                it
            } else {
                null
            }
        }
        if (addedChannels.isEmpty()) return

        McPlayerRegisterChannelsEvent.invoker.onPlayerRegisterChannels(mcServerPlayer, addedChannels)
    }

    private fun hasForgeChannel(channels: List<String>): Boolean =
        "fml:handshake" in channels || "forge:handshake" in channels

    //#if FABRIC
    //#if MC>=12002
    //$$ object ConfigHandler : S2CConfigurationChannelEvents.Register {
    //$$     override fun onChannelRegister(
    //$$         handler: ServerConfigurationPacketListenerImpl,
    //$$         sender: PacketSender,
    //$$         server: MinecraftServer,
    //$$         channels: MutableList<ResourceLocation>
    //$$     ) {
    //$$         channelsCache.put(handler.owner.id, channels.map { it.toString() })
    //$$     }
    //$$ }
    //#endif
    //#endif
}
