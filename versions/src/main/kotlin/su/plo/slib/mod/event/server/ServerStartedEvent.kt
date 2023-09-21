package su.plo.slib.mod.event.server

import net.minecraft.server.MinecraftServer
import su.plo.slib.api.event.GlobalEvent

/**
 * This event is fired once server has started
 */
object ServerStartedEvent
    : GlobalEvent<ServerStartedEvent.Callback>(
    { callbacks ->
        Callback { minecraftServer ->
            callbacks.forEach { callback -> callback.onServerStarted(minecraftServer) }
        }
    }
) {
    fun interface Callback {

        fun onServerStarted(minecraftServer: MinecraftServer)
    }
}
