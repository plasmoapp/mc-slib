package su.plo.slib.mod.channel

import net.minecraft.server.level.ServerPlayer
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent
import su.plo.slib.mod.extension.toMcServerPlayer
import su.plo.slib.mod.entity.ModServerPlayer
import su.plo.slib.mod.ModServerLib

//? if fabric {
import com.google.common.cache.CacheBuilder
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.fabricmc.fabric.api.networking.v1.S2CConfigurationChannelEvents
import su.plo.slib.api.event.player.McPlayerJoinEvent

import java.util.UUID
import java.util.concurrent.TimeUnit
//?} else {
/*import io.netty.util.AttributeKey
import net.minecraft.network.ConnectionProtocol
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup
import net.neoforged.neoforge.network.registration.NetworkRegistry
*///?}

object RegisterChannelHandler
    //? if fabric {
    : S2CPlayChannelEvents.Register
    //?}
{
    //? if fabric {
    val channelsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1L, TimeUnit.MINUTES)
        .build<UUID, List<String>>()

    init {
        McPlayerJoinEvent.registerListener {
            val channels = channelsCache.getIfPresent(it.uuid) ?: return@registerListener
            channelsCache.invalidate(it.uuid)

            firePlayerRegisterChannels(it.getInstance(), channels)
        }
    }

    override fun onChannelRegister(
        handler: ServerGamePacketListenerImpl,
        sender: PacketSender,
        server: MinecraftServer,
        newChannels: MutableList<ResourceLocation>
    ) {
        // neoforge sends channels only on configuration state,
        // so we need to save these channels and check them on player join
        val newChannels = newChannels.map { channel -> channel.toString() }

        val player = handler.player
        val channels = channelsCache.getIfPresent(player.uuid)
            ?.let { it + newChannels }
            ?.toSet()
            ?.toList()
            ?: newChannels
        channelsCache.invalidate(player.uuid)

        firePlayerRegisterChannels(player, channels)
    }
    //?} else {
    /*private val ATTRIBUTE_PAYLOAD_SETUP = AttributeKey.valueOf<NetworkPayloadSetup>("neoforge:payload_setup")

    @SubscribeEvent
    fun onPlayerJoin(event: PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return

        val payloadSetup = player.connection.connection.channel()
            .attr(ATTRIBUTE_PAYLOAD_SETUP)
            .get()
            ?: return

        val channels = ConnectionProtocol.entries
            .flatMap { payloadSetup.getChannels(it).keys }
            .takeIf { it.isNotEmpty() }
            ?.map { it.toString() }
            ?: return

        firePlayerRegisterChannels(player, channels)
    }
    *///?}

    fun firePlayerRegisterChannels(player: ServerPlayer, channels: List<String>) {
        // skip player if he's not placed in the playerlist yet
        if (ModServerLib.minecraftServer.playerList.getPlayer(player.uuid) == null) return

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

    //? if fabric {
    object ConfigHandler : S2CConfigurationChannelEvents.Register {
        override fun onChannelRegister(
            handler: ServerConfigurationPacketListenerImpl,
            sender: PacketSender,
            server: MinecraftServer,
            channels: MutableList<ResourceLocation>
        ) {
            channelsCache.put(handler.owner.id, channels.map { it.toString() })
        }
    }
    //?}
}
