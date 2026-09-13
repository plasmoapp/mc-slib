package su.plo.slib.mod.channel

import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import net.minecraft.network.Connection
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus

//? if fabric {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
//?} else {
/*import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
*///?}

@ApiStatus.Experimental
object ModClientChannelManager {
    private val handlers: SetMultimap<ResourceLocation, ModClientChannelHandler> =
        Multimaps.synchronizedSetMultimap(Multimaps.newSetMultimap(HashMap(), ::HashSet))

    private val receivers: MutableSet<ResourceLocation> = HashSet()

    @Synchronized
    fun registerHandler(channelKey: ResourceLocation, handler: ModClientChannelHandler) {
        handlers.put(channelKey, handler)

        if (!receivers.add(channelKey)) return

        //? if fabric {
        val codec = ModChannelManager.getOrRegisterCodec(channelKey)

        ClientPlayNetworking.registerGlobalReceiver(codec.type) { payload, context ->
            receive(channelKey, payload.data, context.player().connection.connection)
        }
        //?} else {
        /*ModChannelManager.setClientHandler(channelKey) { payload, context ->
            receive(channelKey, payload.data, context.connection())
        }
        *///?}
    }

    @Synchronized
    fun unregisterHandler(channelKey: ResourceLocation, handler: ModClientChannelHandler) {
        handlers.remove(channelKey, handler)
    }

    fun sendPacket(channelKey: ResourceLocation, data: ByteArray, connection: Connection) {
        //? if fabric {
        val codec = ModChannelManager.getOrRegisterCodec(channelKey)
        val packet = ClientPlayNetworking.createC2SPacket(ByteArrayPayload(codec.type, data))
        //?} else {

        /*val codec = ModChannelManager.getOrRegisterCodec(channelKey)
        val packet = ServerboundCustomPayloadPacket(ByteArrayPayload(codec.type, data))

        *///?}

        connection.send(packet)
    }

    internal fun receive(channelKey: ResourceLocation, data: ByteArray, connection: Connection) {
        handlers.get(channelKey)
            .forEach { handler -> handler.receive(data, connection) }
    }
}
