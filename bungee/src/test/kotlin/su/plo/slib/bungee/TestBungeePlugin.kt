package su.plo.slib.bungee

import net.md_5.bungee.api.plugin.Plugin
import su.plo.slib.proxy.TestProxy

class TestBungeePlugin : Plugin() {
    private val testProxy = TestProxy()

    override fun onEnable() {
        val minecraftServer = BungeeProxyLib(this)
    }
}
