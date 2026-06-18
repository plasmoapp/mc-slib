//? if fabric {
package su.plo.slib.mod

import net.fabricmc.api.ModInitializer
import su.plo.slib.mod.event.ModServerEvents
import su.plo.slib.mod.event.server.ServerStartedEvent
import su.plo.slib.server.TestServer

class TestFabricMod : ModInitializer {
    private val testServer = TestServer(ModServerLib)

    override fun onInitialize() {
        ModServerEvents.initialize()

        ServerStartedEvent.registerListener {
            testServer.onEnable()
        }
    }
}
//?}
