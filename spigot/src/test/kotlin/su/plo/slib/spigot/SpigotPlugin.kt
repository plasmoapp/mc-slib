package su.plo.slib.spigot

import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.server.TestServer

class SpigotPlugin : JavaPlugin() {

    private val minecraftServer = SpigotServerLib(this)
    private val testServer = TestServer(minecraftServer)

    override fun onEnable() {
        minecraftServer.onInitialize()
        testServer.registerChannels()
    }

    override fun onDisable() {
        minecraftServer.onShutdown()
    }
}
