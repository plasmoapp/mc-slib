package su.plo.slib.mod.event.server

import net.minecraft.server.MinecraftServer
import su.plo.slib.api.event.GlobalEvent

/**
 * This event is fired when server is stopping
 */
object ServerStoppingEvent
    : GlobalEvent<ServerStoppingEvent.Callback>(
    { callbacks ->
        Callback { minecraftServer ->
            callbacks.forEach { callback -> callback.onServerStopping(minecraftServer) }
        }
    }
) {
    fun interface Callback {

        fun onServerStopping(minecraftServer: MinecraftServer)
    }
}
