package su.plo.slib.mod.mixinkt

//#if FORGE
//#if MC<12002
//$$ import io.netty.util.AsciiString
//$$ import net.minecraft.network.FriendlyByteBuf
//$$ import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket
//$$ import net.minecraft.resources.ResourceLocation
//$$ import net.minecraft.server.level.ServerPlayer
//$$ import su.plo.slib.mod.channel.RegisterChannelHandler
//$$
//$$ object MixinServerGamePacketListenerImplKt {
//$$
//$$     private val REGISTER = ResourceLocation.tryParse("minecraft:register")
//$$
//$$     fun handleCustomPayload(player: ServerPlayer, packet: ServerboundCustomPayloadPacket) {
//$$         val packetId = packet.identifier
//$$         if (packetId != REGISTER) return
//$$
//$$         val channels = parseChannels(packet.data)
//$$         RegisterChannelHandler.firePlayerRegisterChannels(player, channels)
//$$     }
//$$
//$$     private fun parseChannels(buf: FriendlyByteBuf): List<String> {
//$$         val channels = ArrayList<String>()
//$$         val active = StringBuilder()
//$$
//$$         while (buf.isReadable) {
//$$             val byte = buf.readByte()
//$$
//$$             if (byte != 0.toByte()) {
//$$                 active.append(AsciiString.b2c(byte))
//$$                 continue
//$$             }
//$$
//$$             val channel = active.toString()
//$$             if (ResourceLocation.isValidResourceLocation(channel)) {
//$$                 channels.add(channel)
//$$             }
//$$
//$$             active.clear()
//$$         }
//$$
//$$         return channels
//$$     }
//$$ }
//#endif
//#endif
