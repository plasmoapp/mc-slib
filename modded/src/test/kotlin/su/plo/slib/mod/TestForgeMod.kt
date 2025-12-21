//? if forge {
/*package su.plo.slib.mod

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import su.plo.slib.mod.channel.ModChannelManager
import su.plo.slib.mod.event.ModServerEvents
import su.plo.slib.server.TestServer

//? if >=1.20.2 {
/^import net.minecraftforge.network.ChannelBuilder
^///?} else {
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder
//?}

@Mod("slib_test_mod")
class TestForgeMod {
    private val testServer = TestServer(ModServerLib)
    private val channelKey = ResourceLocation.tryParse(testServer.channelKey)!!

    init {
        FMLJavaModLoadingContext.get().modEventBus.register(this)
        //? if >=1.20.5 {
        /^ModChannelManager.getOrRegisterCodec(TestMod.channelKey)
        ^///?}
    }

    @SubscribeEvent
    fun FMLCommonSetupEvent.onCommonSetup() {
        val channel = ChannelBuilder.named(channelKey)
            //? if >=1.20.2 {
            /^.optional()
            ^///?} else {
            .networkProtocolVersion { NetworkRegistry.ACCEPTVANILLA }
            .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA))
            .serverAcceptedVersions(NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA))
            //?}
            .eventNetworkChannel()

        ModChannelManager.addForgeChannel(channelKey, channel)

        ModServerEvents.initialize()
        testServer.registerChannels()
    }
}
*///?}
