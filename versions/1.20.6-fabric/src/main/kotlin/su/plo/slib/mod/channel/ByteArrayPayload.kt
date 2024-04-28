package su.plo.slib.mod.channel

import net.minecraft.network.protocol.common.custom.CustomPacketPayload

class ByteArrayPayload(
    private val type: CustomPacketPayload.Type<ByteArrayPayload>,
    val data: ByteArray
) : CustomPacketPayload {

    override fun type(): CustomPacketPayload.Type<ByteArrayPayload> = type
}
