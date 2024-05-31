package su.plo.slib.mod.channel

import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import io.netty.buffer.ByteBufUtil
import net.minecraft.resources.ResourceLocation
import su.plo.slib.api.server.channel.McServerChannelHandler
import su.plo.slib.api.server.channel.McServerChannelManager
import su.plo.slib.mod.extension.toMcServerPlayer
import java.util.*

//#if FABRIC

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

//#if MC>=12005
//$$ import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
//#endif

//#elseif NEOFORGE

//$$ import net.minecraft.server.level.ServerPlayer
//$$ import net.neoforged.bus.api.SubscribeEvent
//$$ import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
//$$ import net.neoforged.neoforge.network.handling.IPayloadHandler

//#else

//$$ import net.minecraftforge.network.NetworkDirection
//$$ import net.minecraftforge.network.NetworkEvent
//$$ import net.minecraftforge.network.event.EventNetworkChannel

//#if MC>=12002
//$$ import net.minecraftforge.network.ChannelBuilder
//#else
//$$ import net.minecraftforge.network.NetworkRegistry
//$$ import net.minecraftforge.network.NetworkRegistry.ChannelBuilder
//#endif

//#endif

class ModChannelManager : McServerChannelManager {

    private val internalHandlers: SetMultimap<ResourceLocation, McServerChannelHandler> =
        Multimaps.newSetMultimap(HashMap(), ::HashSet)

    override val registeredChannels: MutableSet<String> = HashSet()

    //#if NEOFORGE
    //$$ @SubscribeEvent
    //$$ fun onRegisterPayloadHandlers(event: RegisterPayloadHandlersEvent) {
    //$$     val registrar = event.registrar("0").optional()
    //$$
    //$$     println("Register codecs: $codecs")
    //$$     codecs.forEach { (channelKey, codec) ->
    //$$         registrar.playBidirectional(
    //$$             codec.type,
    //$$             codec
    //$$         ) { payload, context ->
    //$$             val player = context.player()
    //$$             if (player !is ServerPlayer) {
    //$$                 clientHandlers[channelKey]?.handle(payload, context)
    //$$                 return@playBidirectional
    //$$             }
    //$$
    //$$             internalHandlers.get(channelKey)
    //$$                 .forEach { channelHandler ->
    //$$                     channelHandler.receive(player.toMcServerPlayer(), payload.data)
    //$$                 }
    //$$         }
    //$$     }
    //$$ }
    //#endif

    @Synchronized
    override fun registerChannelHandler(channel: String, handler: McServerChannelHandler) {
        val channelKey = ResourceLocation.tryParse(channel) ?: throw IllegalArgumentException("Invalid channel key")

        if (internalHandlers.containsKey(channelKey) || registeredChannels.contains(channel)) {
            internalHandlers.put(channelKey, handler)
            return
        } else {
            registeredChannels.add(channel)
            internalHandlers.put(channelKey, handler)
        }

        //#if FABRIC

        //#if MC>=12005
        //$$ val codec = getOrRegisterCodec(channelKey)
        //$$
        //$$ ServerPlayNetworking.registerGlobalReceiver(codec.type) { payload, context ->
        //$$     internalHandlers.get(channelKey)
        //$$         .forEach { channelHandler ->
        //$$             channelHandler.receive(context.player().toMcServerPlayer(), payload.data)
        //$$         }
        //$$ }
        //#else
        ServerPlayNetworking.registerGlobalReceiver(channelKey) { _, player, _, buf, _ ->
            val messageBytes = ByteBufUtil.getBytes(buf)

            internalHandlers.get(channelKey)
                .forEach { channelHandler ->
                    channelHandler.receive(player.toMcServerPlayer(), messageBytes)
                }
        }
        //#endif

        //#elseif FORGE
        //$$ val forgeChannel = channels.computeIfAbsent(channelKey) {
        //$$     ChannelBuilder.named(channelKey)
        //#if MC>=12002
        //$$         .optional()
        //#else
        //$$         .networkProtocolVersion { NetworkRegistry.ACCEPTVANILLA }
        //$$         .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA))
        //$$         .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA))
        //#endif
        //$$         .eventNetworkChannel()
        //$$ }

        //$$ forgeChannel.addListener<NetworkEvent> { event ->
        //#if MC>=12002
        //$$     val context = event.source
        //#else
        //$$     val context = event.source.get()
        //#endif
        //$$ if (
        //#if MC>=12005
        //$$     context.isClientSide ||
        //#else
        //$$     context.direction != NetworkDirection.PLAY_TO_SERVER ||
        //#endif
        //$$     event.payload == null
        //$$ ) return@addListener

        //$$     val messageBytes = ByteBufUtil.getBytes(event.payload)

        //$$     internalHandlers.get(channelKey)
        //$$         .forEach { channelHandler ->
        //$$             channelHandler.receive(context.sender!!.toMcServerPlayer(), messageBytes)
        //$$         }
        //$$ }
        //#endif
    }

    override fun unregisterChannelHandler(channel: String, handler: McServerChannelHandler) {
        internalHandlers.remove(channel, handler)
    }

    override fun clear() {
        internalHandlers.clear()
    }

    companion object {
        //#if NEOFORGE
        //$$ private val clientHandlers: MutableMap<ResourceLocation, IPayloadHandler<ByteArrayPayload>> = HashMap()
        //$$
        //$$ fun registerClientHandler(channel: ResourceLocation, handler: IPayloadHandler<ByteArrayPayload>) {
        //$$     clientHandlers[channel] = handler
        //$$ }
        //#endif

        //#if MC>=12005
        //$$ private val codecs: MutableMap<ResourceLocation, ByteArrayCodec> = HashMap()
        //$$
        //$$ fun getOrRegisterCodec(channelKey: ResourceLocation): ByteArrayCodec = codecs.computeIfAbsent(channelKey) {
        //$$     println("Register $channelKey")
        //$$     ByteArrayCodec(channelKey)
        //#if FABRIC
        //$$         .also {
        //$$             PayloadTypeRegistry.playC2S().register(it.type, it)
        //$$             PayloadTypeRegistry.playS2C().register(it.type, it)
        //$$         }
        //#endif
        //$$ }
        //#endif

        //#if FORGE
        //$$  private val channels: MutableMap<ResourceLocation, EventNetworkChannel> = HashMap()
        //$$
        //$$  @JvmStatic
        //$$  fun getForgeChannel(channelKey: ResourceLocation): EventNetworkChannel? =
        //$$      channels[channelKey]
        //$$
        //$$  /**
        //$$   * You can add your own forge channel here, if you are using channel for client-side
        //$$   */
        //$$  @JvmStatic
        //$$  fun addForgeChannel(channelKey: ResourceLocation, channel: EventNetworkChannel) {
        //$$      channels[channelKey] = channel
        //$$  }
        //#endif
    }
}
