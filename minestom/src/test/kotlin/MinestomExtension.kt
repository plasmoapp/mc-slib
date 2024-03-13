import net.minestom.server.extensions.Extension
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.minestom.MinestomServerLib

class MinestomExtension : Extension() {

    init {
        McServerCommandsRegisterEvent.registerListener { _, _ ->
            // register commands here
            // commandManager.register("pepega", PepegaCommand())
        }
    }

    private val minecraftServerLib = MinestomServerLib(this)

    override fun initialize() {
        minecraftServerLib.onInitialize()
    }

    override fun terminate() {
        minecraftServerLib.onShutdown()
    }
}
