//#if MC>=12005
//$$ package su.plo.slib.mod.channel
//$$
//$$ import net.minecraft.network.RegistryFriendlyByteBuf
//$$ import net.minecraft.network.codec.StreamCodec
//$$ import net.minecraft.network.protocol.common.custom.CustomPacketPayload
//$$ import net.minecraft.resources.ResourceLocation
//$$
//$$ class ByteArrayCodec(
//$$     channelKey: ResourceLocation
//$$ ) : StreamCodec<RegistryFriendlyByteBuf, ByteArrayPayload> {
//$$
//$$     val type = CustomPacketPayload.Type<ByteArrayPayload>(channelKey)
//$$
//$$     override fun decode(buf: RegistryFriendlyByteBuf): ByteArrayPayload {
//$$         val length = buf.readableBytes()
//$$
//$$         val data = ByteArray(length)
//$$         buf.readBytes(data)
//$$
//$$         return ByteArrayPayload(type, data)
//$$     }
//$$
//$$     override fun encode(buf: RegistryFriendlyByteBuf, payload: ByteArrayPayload) {
//$$         buf.writeBytes(payload.data)
//$$     }
//$$ }
//#endif