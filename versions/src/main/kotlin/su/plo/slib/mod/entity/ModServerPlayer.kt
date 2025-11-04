package su.plo.slib.mod.entity

import com.google.common.collect.Sets
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.mod.chat.ComponentTextConverter
import su.plo.slib.mod.extension.getObjectiveBelowName
import su.plo.slib.mod.extension.toMcGameProfile
import su.plo.slib.mod.extension.serverLevel
import su.plo.slib.permission.PermissionSupplier

//#if FABRIC

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

//#elseif FORGE

//$$ import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket

//#if MC>=12002
//$$ import net.minecraftforge.network.NetworkDirection
//#endif

//#elseif NEOFORGE

//$$ import net.neoforged.neoforge.common.extensions.ICommonPacketListener
//$$ import net.neoforged.neoforge.network.registration.NetworkRegistry

//#endif

//#if MC>=11701
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
//#else
//$$ import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket
//#endif

//#if MC<11900
//$$ import net.minecraft.Util
//#endif

//#if MC>=12005
//$$ import su.plo.slib.mod.channel.ModChannelManager

//#if FORGE
//$$ import net.minecraft.network.protocol.common.ClientCommonPacketListener
//#else
//$$ import su.plo.slib.mod.channel.ByteArrayPayload
//#endif

//#endif

class ModServerPlayer(
    private val minecraftServer: McServerLib,
    private val permissions: PermissionSupplier,
    player: ServerPlayer
) : ModServerEntity<ServerPlayer>(minecraftServer, player), McServerPlayer {

    override val gameProfile: McGameProfile
        get() = instance.gameProfile.toMcGameProfile()

    override val name: String
        get() = instance.gameProfile.name

    override val isSpectator: Boolean
        get() = instance.isSpectator

    override val isSneaking: Boolean
        get() = instance.isDescending

    override val hasLabelScoreboard: Boolean
        get() = instance.serverLevel().scoreboard.getObjectiveBelowName() != null

    override val isOnline: Boolean
        get() = !instance.hasDisconnected()

    override var language: String = "en_us"

    override val registeredChannels: MutableSet<String> = Sets.newCopyOnWriteArraySet()

    override val spectatorTarget: McServerEntity? = null
        get() {
            if (instance.camera === instance) {
                return null
            } else if (field == null || !instance.camera.equals(field.getInstance())) {
                return minecraftServer.getEntityByInstance(instance.camera)
            }

            return field
        }

    override fun canSee(player: McServerPlayer): Boolean {
        val serverPlayer = player.getInstance<ServerPlayer>()

        return if (serverPlayer.isSpectator) {
            instance.isSpectator
        } else true
    }

    override fun getPermission(permission: String) =
        permissions.getPermission(instance, permission)

    override fun hasPermission(permission: String) =
        permissions.hasPermission(instance, permission)

    override fun kick(reason: McTextComponent) {
        val json = minecraftServer.textConverter.convertToJson(this, reason)
        val component = ComponentTextConverter.convertFromJson(json)

        instance.connection.disconnect(component)
    }

    override fun sendActionBar(text: McTextComponent) {
        val json = minecraftServer.textConverter.convertToJson(this, text)
        val component = ComponentTextConverter.convertFromJson(json)

        //#if MC>=11701
        instance.connection.send(
            ClientboundSetActionBarTextPacket(component)
        )
        //#else
        //$$ instance.connection.send(
        //$$     ClientboundSetTitlesPacket(
        //$$         ClientboundSetTitlesPacket.Type.ACTIONBAR,
        //$$         component
        //$$     )
        //$$ )
        //#endif
    }

    override fun sendMessage(text: McTextComponent) {
        val json = minecraftServer.textConverter.convertToJson(this, text)
        val component = ComponentTextConverter.convertFromJson(json)

        //#if MC>=11900
        instance.sendSystemMessage(component)
        //#else
        //$$ instance.sendMessage(component, Util.NIL_UUID);
        //#endif
    }

    override fun sendPacket(channel: String, data: ByteArray) {
        val channelKey = ResourceLocation.tryParse(channel) ?: throw IllegalArgumentException("Invalid channel key")
        //#if MC<12005
        val buf = FriendlyByteBuf(Unpooled.wrappedBuffer(data))
        //#endif

        //#if FABRIC

        //#if MC>=12005
        //$$ val codec = ModChannelManager.getOrRegisterCodec(channelKey)
        //$$ ServerPlayNetworking.send(instance, ByteArrayPayload(codec.type, data))
        //#else
        ServerPlayNetworking.send(instance, channelKey, buf)
        //#endif

        //#elseif FORGE

        //#if MC>=12006
        //$$ val forgeChannel = ModChannelManager.getForgeChannel(channelKey)
        //$$ val buf = FriendlyByteBuf(Unpooled.wrappedBuffer(data))
        //$$
        //$$ val packet = NetworkDirection.PLAY_TO_CLIENT
        //$$     .buildPacket<ClientCommonPacketListener, FriendlyByteBuf>(forgeChannel, buf)
        //#elseif MC>=12002
        //$$ val packet = NetworkDirection.PLAY_TO_CLIENT
        //$$     .buildPacket<ClientboundCustomPayloadPacket>(buf, channelKey)
        //$$     .getThis()
        //#else
        //$$ val packet = ClientboundCustomPayloadPacket(channelKey, buf)
        //#endif
        //$$ instance.connection.send(packet)

        //#elseif NEOFORGE

        //$$ // hack to avoid neoforge channels check
        //$$ if (!NetworkRegistry.hasChannel(instance.connection as ICommonPacketListener, channelKey)) {
        //$$     NetworkRegistry.onMinecraftRegister(
        //$$         instance.connection.connection,
        //$$         setOf(channelKey)
        //$$     )
        //$$ }
        //$$
        //$$ val codec = ModChannelManager.getOrRegisterCodec(channelKey)
        //$$ instance.connection.send(ByteArrayPayload(codec.type, data))

        //#endif
    }

    fun addChannel(channel: String) =
        registeredChannels.add(channel)
}
