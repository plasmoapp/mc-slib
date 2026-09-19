package su.plo.slib.mod.channel

import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import net.minecraft.resources.ResourceLocation
import su.plo.slib.api.server.channel.McServerChannelHandler
import su.plo.slib.api.server.channel.McServerChannelManager
import su.plo.slib.mod.extension.toMcServerPlayer
import java.util.*

//? if fabric {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
//?} elif neoforge {
/*import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadHandler
import net.neoforged.neoforge.network.registration.PayloadRegistrar
*///?}

//? if neoforge {
/*import su.plo.slib.api.logging.McLoggerFactory
import su.plo.slib.api.server.channel.McServerChannelRegistry
*///?}

class ModChannelManager : McServerChannelManager {

    private val internalHandlers: SetMultimap<ResourceLocation, McServerChannelHandler> =
        Multimaps.newSetMultimap(HashMap(), ::HashSet)

    override val registeredChannels: MutableSet<String> = HashSet()

    //? if neoforge {
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
        val codec = getOrRegisterCodec(channelKey)

        ServerPlayNetworking.registerGlobalReceiver(codec.type) { payload, context ->
            internalHandlers.get(channelKey)
                .forEach { channelHandler ->
                    channelHandler.receive(context.player().toMcServerPlayer(), payload.data)
                }
        }
        //?} else {
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
        private val codecs: MutableMap<ResourceLocation, ByteArrayCodec> = HashMap()

        fun getOrRegisterCodec(channelKey: ResourceLocation): ByteArrayCodec = codecs.computeIfAbsent(channelKey) {
            ByteArrayCodec(channelKey)
                //? if fabric {
                .also {
                    PayloadTypeRegistry.playC2S().register(it.type, it)
                    PayloadTypeRegistry.playS2C().register(it.type, it)
                }
                //?}
        }

        //? if neoforge {
        /*private val logger = McLoggerFactory.createLogger("ModChannelManager")

        internal var instance: ModChannelManager? = null

        private val lateChannelMessage =
            """
                Channel {} is not registered in the mod loader networking.
                Register it with McServerChannelRegistry.register from your mod entrypoint.
            """.trim().lines().joinToString(" ")

        private val clientHandlers: MutableMap<ResourceLocation, IPayloadHandler<ByteArrayPayload>> = HashMap()
        private val registeredPayloads: MutableSet<ResourceLocation> = HashSet()

        private var channelsRegistered = false

        @Deprecated("Use ModClientChannelManager.registerHandler, it works on any loader and version")
        fun registerClientHandler(channel: ResourceLocation, handler: IPayloadHandler<ByteArrayPayload>) {
            setClientHandler(channel, handler)
        }

        internal fun setClientHandler(channel: ResourceLocation, handler: IPayloadHandler<ByteArrayPayload>) {
            clientHandlers[channel] = handler
            warnIfChannelNotRegistered(channel)
        }

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
    }
}
