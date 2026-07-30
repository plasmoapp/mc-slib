//? if neoforge {
/*package su.plo.slib.mod

import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import su.plo.slib.api.server.channel.McServerChannelRegistry
import su.plo.slib.mod.event.ModServerEvents
import su.plo.slib.mod.event.server.ServerStartedEvent
import su.plo.slib.server.TestServer

@Mod("slib_test_mod")
class TestNeoForgeMod(
    modBus: IEventBus,
) {
    private var testServer = TestServer(ModServerLib)

    init {
        modBus.register(this)
        modBus.register(ModServerLib.channelManager)
        McServerChannelRegistry.register(testServer.channelKey)
    }

    @SubscribeEvent
    fun FMLCommonSetupEvent.onCommonSetup() {
        ModServerEvents.initialize()
        ServerStartedEvent.registerListener {
            testServer.onEnable()
        }
    }
}
*///?}
