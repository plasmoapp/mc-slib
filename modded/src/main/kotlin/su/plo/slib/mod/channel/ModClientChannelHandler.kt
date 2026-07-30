package su.plo.slib.mod.channel

import net.minecraft.network.Connection
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Experimental
fun interface ModClientChannelHandler {
    fun receive(data: ByteArray, connection: Connection)
}
