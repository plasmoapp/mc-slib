//? if neoforge {
/*package su.plo.slib.mod

import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import su.plo.slib.mod.channel.ModChannelManager
import su.plo.slib.mod.event.ModServerEvents
import su.plo.slib.server.TestServer

@Mod("slib_test_mod")
class TestNeoForgeMod(
    modBus: IEventBus,
) {
    private var testServer = TestServer(ModServerLib)
    private val channelKey = ResourceLocation.tryParse(testServer.channelKey)!!

    init {
        modBus.register(this)
        ModChannelManager.getOrRegisterCodec(channelKey)
    }

    @SubscribeEvent
    fun FMLCommonSetupEvent.onCommonSetup() {
        ModServerEvents.initialize()
        testServer.registerChannels()
    }
}
*///?}
