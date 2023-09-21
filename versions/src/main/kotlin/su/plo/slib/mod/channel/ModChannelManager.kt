package su.plo.slib.mod.channel

import com.google.common.collect.ListMultimap
import com.google.common.collect.Multimaps
import io.netty.buffer.ByteBufUtil
import net.minecraft.resources.ResourceLocation
import su.plo.slib.api.server.channel.McChannelHandler
import su.plo.slib.api.server.channel.McChannelManager
import su.plo.slib.mod.extension.mustToMcServerPlayer
import java.util.*

//#if FABRIC
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
//#else
//$$ import net.minecraftforge.network.NetworkDirection
//$$ import net.minecraftforge.network.NetworkEvent
//$$ import net.minecraftforge.network.NetworkRegistry
//$$ import net.minecraftforge.network.event.EventNetworkChannel
//#endif

class ModChannelManager : McChannelManager {

    private val internalHandlers: ListMultimap<ResourceLocation, McChannelHandler> =
        Multimaps.newListMultimap(HashMap(), ::LinkedList)

    @Synchronized
    override fun registerChannelHandler(channel: String, handler: McChannelHandler) {
        val channelKey = ResourceLocation(channel)

        if (internalHandlers.containsKey(channelKey)) {
            internalHandlers.put(channelKey, handler)
            return
        }

        //#if FABRIC
        ServerPlayNetworking.registerGlobalReceiver(channelKey) { _, player, _, buf, _ ->
            val messageBytes = ByteBufUtil.getBytes(buf)

            internalHandlers.get(channelKey)
                .forEach { channelHandler ->
                    channelHandler.receive(player.mustToMcServerPlayer(), messageBytes)
                }
        }
        //#else
        //$$ val forgeChannel = channels.computeIfAbsent(channelKey) {
        //$$     NetworkRegistry.newEventChannel(
        //$$         channelKey,
        //$$         { NetworkRegistry.ACCEPTVANILLA },
        //$$         NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA),
        //$$         NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA)
        //$$     )
        //$$ }

        //$$ forgeChannel.addListener<NetworkEvent> { event ->
        //$$     val context = event.source.get()
        //$$     if (context.direction != NetworkDirection.PLAY_TO_SERVER || event.payload == null) return@addListener

        //$$     val messageBytes = ByteBufUtil.getBytes(event.payload)

        //$$     internalHandlers.get(channelKey)
        //$$         .forEach { channelHandler ->
        //$$             channelHandler.receive(context.sender!!.mustToMcServerPlayer(), messageBytes)
        //$$         }
        //$$ }
        //#endif
    }

    //#if FORGE
    //$$ companion object {
    //$$    private val channels: MutableMap<ResourceLocation, EventNetworkChannel> = HashMap()

    //$$    /**
    //$$     * You can add your own forge channel here, if you are using channel for client-side
    //$$     */
    //$$    fun addForgeChannel(channelKey: ResourceLocation, channel: EventNetworkChannel) {
    //$$        channels[channelKey] = channel
    //$$    }
    //$$ }
    //#endif
}
