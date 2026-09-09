package su.plo.slib.paper

import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.server.TestServer

class PaperPlugin : JavaPlugin() {
    private val minecraftServer = PaperServerLib(this, LOGGER)
    private val testServer = TestServer(minecraftServer)

    override fun onEnable() {
        minecraftServer.onInitialize()
        testServer.onEnable()
    }

    override fun onDisable() {
        minecraftServer.onShutdown()
    }
}
