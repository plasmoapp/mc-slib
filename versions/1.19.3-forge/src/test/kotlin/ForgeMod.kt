import net.minecraft.resources.ResourceLocation
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent
import su.plo.slib.mod.ModServerLib
import su.plo.slib.mod.channel.ModChannelManager
import su.plo.slib.mod.event.ModServerEvents
import su.plo.slib.mod.event.server.ServerStartedEvent

//#if MC>=12002
//$$ import net.minecraftforge.network.ChannelBuilder
//#else
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder
//#endif

@Mod("test")
class ForgeMod {

    init {
        FMLJavaModLoadingContext.get().modEventBus.register(this)

        McServerCommandsRegisterEvent.registerListener { commandManager, minecraftServer ->
            // register commands here
            // commandManager.register("pepega", PepegaCommand())
        }
    }

    @SubscribeEvent
    fun onCommonSetup(event: FMLCommonSetupEvent) {
        // if you are using McChannelManager, you need to register EventNetworkChannels in ModChannelManager
        val channelLocation = ResourceLocation("pepega:clap")
        val channel = ChannelBuilder.named(channelLocation).eventNetworkChannel()
        ModChannelManager.addForgeChannel(channelLocation, channel)

        // initializes default mod server events
        ModServerEvents.initialize()

        // after MinecraftServer initialization, you can access ModServerLib by static instance:
        ServerStartedEvent.registerListener {
            val serverLib = ModServerLib
        }
    }
}
