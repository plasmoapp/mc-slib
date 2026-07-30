package su.plo.slib.mod.channel

import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus

//? if fabric {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

//?} elif neoforge {
/*import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket
*///?} elif forge {
/*import net.minecraftforge.network.NetworkDirection
//? if >=1.20.6 {
/^import net.minecraft.network.protocol.common.ServerCommonPacketListener
^///?} else {
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket
//?}
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

        //? if >=1.20.5 {
        /*val codec = ModChannelManager.getOrRegisterCodec(channelKey)

        ClientPlayNetworking.registerGlobalReceiver(codec.type) { payload, context ->
            receive(channelKey, payload.data, context.player().connection.connection)
        }
        *///?} else {
        ClientPlayNetworking.registerGlobalReceiver(channelKey) { _, listener, buf, _ ->
            receive(channelKey, ByteBufUtil.getBytes(buf), listener.connection)
        }

        //?}
        //?} elif neoforge {

        /*ModChannelManager.setClientHandler(channelKey) { payload, context ->
            receive(channelKey, payload.data, context.connection())
        }

        *///?} elif forge {

        /*ModChannelManager.getOrCreateForgeChannel(channelKey)

        *///?}
    }

    @Synchronized
    fun unregisterHandler(channelKey: ResourceLocation, handler: ModClientChannelHandler) {
        handlers.remove(channelKey, handler)
    }

    fun sendPacket(channelKey: ResourceLocation, data: ByteArray, connection: Connection) {
        //? if <1.20.5 {
        val buf = FriendlyByteBuf(Unpooled.wrappedBuffer(data))
        //?}

        //? if fabric {

        //? if >=1.20.5 {
        /*val codec = ModChannelManager.getOrRegisterCodec(channelKey)
        val packet = ClientPlayNetworking.createC2SPacket(ByteArrayPayload(codec.type, data))
        *///?} else {
        val packet = ClientPlayNetworking.createC2SPacket(channelKey, buf)
        //?}

        //?} elif neoforge {

        /*val codec = ModChannelManager.getOrRegisterCodec(channelKey)
        val packet = ServerboundCustomPayloadPacket(ByteArrayPayload(codec.type, data))

        *///?} elif forge {

        /*//? if >=1.20.6 {
        /^val forgeChannel = ModChannelManager.getOrCreateForgeChannel(channelKey) ?: return
        val buf = FriendlyByteBuf(Unpooled.wrappedBuffer(data))

        val packet = NetworkDirection.PLAY_TO_SERVER
            .buildPacket<ServerCommonPacketListener, FriendlyByteBuf>(forgeChannel, buf)
        ^///?} elif >=1.20.2 {
        /^val packet = NetworkDirection.PLAY_TO_SERVER
            .buildPacket<ServerboundCustomPayloadPacket>(buf, channelKey)
            .getThis()
        ^///?} else {
        val packet = ServerboundCustomPayloadPacket(channelKey, buf)
        //?}

        *///?}

        connection.send(packet)
    }

    internal fun receive(channelKey: ResourceLocation, data: ByteArray, connection: Connection) {
        handlers.get(channelKey)
            .forEach { handler -> handler.receive(data, connection) }
    }
}
