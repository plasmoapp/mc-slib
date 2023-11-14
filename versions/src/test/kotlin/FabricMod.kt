//#if FABRIC
import net.fabricmc.api.ModInitializer
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.mod.ModServerLib
import su.plo.slib.mod.event.ModServerEvents.Companion.initialize
import su.plo.slib.mod.event.server.ServerStartedEvent

class FabricMod : ModInitializer {

    init {
        McServerCommandsRegisterEvent.registerListener { commandManager, minecraftServer ->
            // register commands here
            // commandManager.register("pepega", PepegaCommand())
        }
    }

    override fun onInitialize() {

        // initializes default mod server events
        initialize()

        // after MinecraftServer initialization, you can access ModServerLib by static instance:
        ServerStartedEvent.registerListener {
            val serverLib = ModServerLib
        }
    }
}
//#endif
