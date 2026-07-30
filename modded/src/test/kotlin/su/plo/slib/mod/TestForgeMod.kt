//? if forge {
/*package su.plo.slib.mod

import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import su.plo.slib.api.server.channel.McServerChannelRegistry
import su.plo.slib.mod.event.ModServerEvents
import su.plo.slib.mod.event.server.ServerStartedEvent
import su.plo.slib.server.TestServer

@Mod("slib_test_mod")
class TestForgeMod {
    private val testServer = TestServer(ModServerLib)

    init {
        val modBus = FMLJavaModLoadingContext.get().modEventBus
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
