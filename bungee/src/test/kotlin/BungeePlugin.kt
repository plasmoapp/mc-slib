import net.md_5.bungee.api.plugin.Plugin
import su.plo.slib.api.proxy.event.command.McProxyCommandsRegisterEvent
import su.plo.slib.bungee.BungeeProxyLib

class BungeePlugin : Plugin() {

    init {
        McProxyCommandsRegisterEvent.registerListener { commandManager, minecraftServer ->
            // register commands here
            // commandManager.register("pepega", PepegaCommand())
        }
    }

    private lateinit var minecraftServer: BungeeProxyLib

    override fun onEnable() {
        // you need to initialize lib here
        minecraftServer = BungeeProxyLib(this)
    }
}
