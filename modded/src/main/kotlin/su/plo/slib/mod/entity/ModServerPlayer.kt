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

//? if fabric {
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
//?} elif forge {
/*import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket
//? if >=1.20.2 {
/^import net.minecraftforge.network.NetworkDirection
^///?}
*///?} elif neoforge {
/*import net.neoforged.neoforge.common.extensions.ICommonPacketListener
import net.neoforged.neoforge.network.registration.NetworkRegistry
*///?}

//? if >=1.17.1 {
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
//?} else {
/*import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket
*///?}

//? if <1.19 {
/*import net.minecraft.Util
*///?}

//? if >=1.20.5 {
/*import su.plo.slib.mod.channel.ModChannelManager
//? if forge {
/^import net.minecraft.network.protocol.common.ClientCommonPacketListener
^///?} else {
import su.plo.slib.mod.channel.ByteArrayPayload
//?}
*///?}

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

        //? if >=1.17.1 {
        instance.connection.send(
            ClientboundSetActionBarTextPacket(component)
        )
        //?} else {
        /*instance.connection.send(
            ClientboundSetTitlesPacket(
                ClientboundSetTitlesPacket.Type.ACTIONBAR,
                component
            )
        )
        *///?}
    }

    override fun sendMessage(text: McTextComponent) {
        val json = minecraftServer.textConverter.convertToJson(this, text)
        val component = ComponentTextConverter.convertFromJson(json)

        //? if >=1.19 {
        instance.sendSystemMessage(component)
        //?} else {
        /*instance.sendMessage(component, Util.NIL_UUID);
        *///?}
    }

    override fun sendPacket(channel: String, data: ByteArray) {
        val channelKey = ResourceLocation.tryParse(channel) ?: throw IllegalArgumentException("Invalid channel key")
        //? if <1.20.5 {
        val buf = FriendlyByteBuf(Unpooled.wrappedBuffer(data))
        //?}

        //? if fabric {
        //? if >=1.20.5 {
        /*val codec = ModChannelManager.getOrRegisterCodec(channelKey)
        ServerPlayNetworking.send(instance, ByteArrayPayload(codec.type, data))
        *///?} else {
        ServerPlayNetworking.send(instance, channelKey, buf)
        //?}

        //?} elif forge {
        
        /*//? if >=1.20.6 {
        /^val forgeChannel = ModChannelManager.getForgeChannel(channelKey)
        val buf = FriendlyByteBuf(Unpooled.wrappedBuffer(data))

        val packet = NetworkDirection.PLAY_TO_CLIENT
            .buildPacket<ClientCommonPacketListener, FriendlyByteBuf>(forgeChannel, buf)
        ^///?} elif >=1.20.2 {
        /^val packet = NetworkDirection.PLAY_TO_CLIENT
            .buildPacket<ClientboundCustomPayloadPacket>(buf, channelKey)
            .getThis()
        ^///?} else {
        val packet = ClientboundCustomPayloadPacket(channelKey, buf)
        //?}
        instance.connection.send(packet)
        *///?} elif neoforge {
        /*// hack to avoid neoforge channels check
        if (!NetworkRegistry.hasChannel(instance.connection as ICommonPacketListener, channelKey)) {
            NetworkRegistry.onMinecraftRegister(
                instance.connection.connection,
                setOf(channelKey)
            )
        }

        val codec = ModChannelManager.getOrRegisterCodec(channelKey)
        instance.connection.send(ByteArrayPayload(codec.type, data))
        *///?}
    }

    fun addChannel(channel: String) =
        registeredChannels.add(channel)
}
