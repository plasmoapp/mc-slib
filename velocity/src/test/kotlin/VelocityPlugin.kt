import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.event.command.McProxyCommandsRegisterEvent
import su.plo.slib.velocity.VelocityProxyLib

@Plugin(
    id = "test",
    name = "Test",
    version = "0.0.1",
    authors = ["Apehum"]
)
class VelocityPlugin @Inject constructor(
    private val proxyServer: ProxyServer
) {

    init {
        McProxyCommandsRegisterEvent.registerListener { commandManager, minecraftServer ->
            // register commands here
            // commandManager.register("pepega", PepegaCommand())
        }
    }

    private lateinit var minecraftProxy: McProxyLib

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        // you need to initialize lib here
        minecraftProxy = VelocityProxyLib(proxyServer, this)
    }
}
