package su.plo.slib.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import su.plo.slib.proxy.TestProxy

@Plugin(
    id = "slib-velocity-test",
    version = "0.0.1",
    authors = ["Apehum"]
)
class TestVelocityPlugin @Inject constructor(
    private val proxyServer: ProxyServer
) {
    private val testProxy = TestProxy()

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        val minecraftProxy = VelocityProxyLib(proxyServer, this)
    }
}
