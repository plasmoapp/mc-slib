import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.spigot.SpigotServerLib

class SpigotPlugin : JavaPlugin() {

    init {
        McServerCommandsRegisterEvent.registerListener { commandManager, minecraftServer ->
            // register commands here
            // commandManager.register("pepega", PepegaCommand())
        }
    }

    private val minecraftServerLib = SpigotServerLib(this)

    override fun onEnable() {
        minecraftServerLib.onInitialize()
    }

    override fun onDisable() {
        minecraftServerLib.onShutdown()
    }
}
