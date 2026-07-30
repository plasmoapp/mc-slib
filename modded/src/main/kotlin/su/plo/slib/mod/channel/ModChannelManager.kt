package su.plo.slib.mod.channel

import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import io.netty.buffer.ByteBufUtil
import net.minecraft.resources.ResourceLocation
import su.plo.slib.api.server.channel.McServerChannelHandler
import su.plo.slib.api.server.channel.McServerChannelManager
import su.plo.slib.mod.extension.toMcServerPlayer
import java.util.*

//? if fabric {
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
//? if >=1.20.5 {
/*import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
*///?}
//?} elif neoforge {
/*import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadHandler
import net.neoforged.neoforge.network.registration.PayloadRegistrar
*///?} elif forge {
/*import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.event.EventNetworkChannel
//? if >=1.20.2 {
/^import net.minecraftforge.network.ChannelBuilder
^///?} else {
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder
//?}
*///?}

//? if forge || neoforge {
/*import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.server.channel.McServerChannelRegistry
*///?}

class ModChannelManager : McServerChannelManager {

    private val internalHandlers: SetMultimap<ResourceLocation, McServerChannelHandler> =
        Multimaps.newSetMultimap(HashMap(), ::HashSet)

    override val registeredChannels: MutableSet<String> = HashSet()

    //? if forge || neoforge {
    /*init {
        instance = this
    }
    *///?}

    //? if neoforge {
    /*@Synchronized
    @SubscribeEvent
    fun onRegisterPayloadHandlers(event: RegisterPayloadHandlersEvent) {
        if (channelsRegistered) return

        val registrar = event.registrar("0").optional()

        val channelKeys = LinkedHashSet<ResourceLocation>(codecs.keys)
        channelKeys.addAll(declaredChannelKeys())

        channelKeys.forEach { channelKey ->
            registerPayload(registrar, channelKey, getOrRegisterCodec(channelKey))
        }

        channelsRegistered = true
    }
    *///?} elif forge {
    /*@Synchronized
    @SubscribeEvent
    fun onCommonSetup(event: FMLCommonSetupEvent) {
        if (channelsRegistered) return

        declaredChannelKeys().forEach { getOrCreateForgeChannel(it) }

        channelsRegistered = true
    }
    *///?}

    @Synchronized
    override fun registerChannelHandler(channel: String, handler: McServerChannelHandler) {
        val channelKey = ResourceLocation.tryParse(channel) ?: throw IllegalArgumentException("Invalid channel key")

        if (internalHandlers.containsKey(channelKey) || registeredChannels.contains(channel)) {
            internalHandlers.put(channelKey, handler)
            return
        } else {
            registeredChannels.add(channel)
            internalHandlers.put(channelKey, handler)
        }

        //? if fabric {
        //? if >=1.20.5 {
        /*val codec = getOrRegisterCodec(channelKey)

        ServerPlayNetworking.registerGlobalReceiver(codec.type) { payload, context ->
            internalHandlers.get(channelKey)
                .forEach { channelHandler ->
                    channelHandler.receive(context.player().toMcServerPlayer(), payload.data)
                }
        }
        *///?} else {
        ServerPlayNetworking.registerGlobalReceiver(channelKey) { _, player, _, buf, _ ->
            val messageBytes = ByteBufUtil.getBytes(buf)

            internalHandlers.get(channelKey)
                .forEach { channelHandler ->
                    channelHandler.receive(player.toMcServerPlayer(), messageBytes)
                }
        }
        //?}
        //?} elif forge {
        /*getOrCreateForgeChannel(channelKey)
        *///?} elif neoforge {
        /*warnIfChannelNotRegistered(channelKey)
        *///?}
    }

    override fun unregisterChannelHandler(channel: String, handler: McServerChannelHandler) {
        internalHandlers.remove(channel, handler)
    }

    override fun clear() {
        internalHandlers.clear()
    }

    companion object {
        //? if forge || neoforge {
        /*private val lateChannelMessage =
            """
                Channel {} is not registered in the mod loader networking.
                Register it with McServerChannelRegistry.register from your mod entrypoint.
            """.trim().lines().joinToString(" ")

        private val logger = McLoggerFactory.createLogger("ModChannelManager")

        internal var instance: ModChannelManager? = null

        private var channelsRegistered = false

        private fun declaredChannelKeys(): Set<ResourceLocation> =
            McServerChannelRegistry.channels()
                .mapNotNullTo(LinkedHashSet<ResourceLocation>()) { ResourceLocation.tryParse(it) }

        private fun warnChannelRegisteredLate(channelKey: ResourceLocation, cause: Throwable? = null) {
            if (cause == null) {
                logger.warn(lateChannelMessage, channelKey)
            } else {
                logger.warn(lateChannelMessage, channelKey, cause)
            }
        }
        *///?}

        //? if neoforge {
        /*private val clientHandlers: MutableMap<ResourceLocation, IPayloadHandler<ByteArrayPayload>> = HashMap()

        private val registeredPayloads: MutableSet<ResourceLocation> = HashSet()

        fun registerClientHandler(channel: ResourceLocation, handler: IPayloadHandler<ByteArrayPayload>) {
            clientHandlers[channel] = handler
            warnIfChannelNotRegistered(channel)
        }

        private fun registerPayload(registrar: PayloadRegistrar, channelKey: ResourceLocation, codec: ByteArrayCodec) {
            registeredPayloads.add(channelKey)
        //? if >=1.21.7 {
            /^registrar.playBidirectional(
                codec.type,
                codec,
                { payload, context ->
                    val player = context.player() as? ServerPlayer ?: return@playBidirectional

                    instance?.internalHandlers?.get(channelKey)
                        ?.forEach { channelHandler ->
                            channelHandler.receive(player.toMcServerPlayer(), payload.data)
                        }
                },
                { payload, context ->
                    clientHandlers[channelKey]?.handle(payload, context)
                }
            )
        ^///?} else {
            registrar.playBidirectional(
                codec.type,
                codec
            ) { payload, context ->
                val player = context.player()
                if (player !is ServerPlayer) {
                    clientHandlers[channelKey]?.handle(payload, context)
                    return@playBidirectional
                }

                instance?.internalHandlers?.get(channelKey)
                    ?.forEach { channelHandler ->
                        channelHandler.receive(player.toMcServerPlayer(), payload.data)
                    }
            }
        //?}
        }

        private fun warnIfChannelNotRegistered(channelKey: ResourceLocation) {
            if (!channelsRegistered || channelKey in registeredPayloads) return

            warnChannelRegisteredLate(channelKey)
        }
        *///?}

        //? if >=1.20.5 {
        /*private val codecs: MutableMap<ResourceLocation, ByteArrayCodec> = HashMap()

        fun getOrRegisterCodec(channelKey: ResourceLocation): ByteArrayCodec = codecs.computeIfAbsent(channelKey) {
            ByteArrayCodec(channelKey)
        //? if fabric {
                .also {
                    PayloadTypeRegistry.playC2S().register(it.type, it)
                    PayloadTypeRegistry.playS2C().register(it.type, it)
                }
        //?}
        }
        *///?}

        //? if forge {
        /*private val channels: MutableMap<ResourceLocation, EventNetworkChannel> = HashMap()

        @JvmStatic
        fun getForgeChannel(channelKey: ResourceLocation): EventNetworkChannel? =
            channels[channelKey]

        /^*
         * You can add your own forge channel here, if you are using channel for client-side
         ^/
        @Deprecated("Use McServerChannelRegistry.register instead")
        @JvmStatic
        fun addForgeChannel(channelKey: ResourceLocation, channel: EventNetworkChannel) {
            if (channels.containsKey(channelKey)) return

            channels[channelKey] = channel
            addChannelListener(channelKey, channel)
        }

        @Synchronized
        private fun getOrCreateForgeChannel(channelKey: ResourceLocation): EventNetworkChannel? {
            channels[channelKey]?.let { return it }

            val channel = try {
                createForgeChannel(channelKey)
            } catch (e: Exception) {
                warnChannelRegisteredLate(channelKey, e)
                return null
            }

            channels[channelKey] = channel
            addChannelListener(channelKey, channel)

            return channel
        }

        private fun createForgeChannel(channelKey: ResourceLocation): EventNetworkChannel =
            ChannelBuilder.named(channelKey)
        //? if >=1.20.2 {
                /^.optional()
        ^///?} else {
                .networkProtocolVersion { NetworkRegistry.ACCEPTVANILLA }
                .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA))
                .serverAcceptedVersions(NetworkRegistry.acceptMissingOr(NetworkRegistry.ACCEPTVANILLA))
        //?}
                .eventNetworkChannel()

        private fun addChannelListener(channelKey: ResourceLocation, channel: EventNetworkChannel) {
            channel.addListener<NetworkEvent> { event ->
            //? if >=1.20.2 {
                /^val context = event.source
            ^///?} else {
                val context = event.source.get()
            //?}
                if (
                //? if >=1.20.5 {
                    /^context.isClientSide ||
                ^///?} else {
                    context.direction != NetworkDirection.PLAY_TO_SERVER ||
                //?}
                    event.payload == null
                ) return@addListener

                val messageBytes = ByteBufUtil.getBytes(event.payload)

                instance?.internalHandlers?.get(channelKey)
                    ?.forEach { channelHandler ->
                        channelHandler.receive(context.sender!!.toMcServerPlayer(), messageBytes)
                    }
            }
        }
        *///?}
    }
}
