package su.plo.slib.mod.entity

import com.google.common.collect.Sets
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.event.player.McPlayerVisibilityCheckEvent
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.mod.channel.ByteArrayPayload
import su.plo.slib.mod.channel.ModChannelManager
import su.plo.slib.mod.chat.ComponentTextConverter
import su.plo.slib.mod.extension.getObjectiveBelowName
import su.plo.slib.mod.extension.toMcGameProfile
import su.plo.slib.mod.extension.serverLevel
import su.plo.slib.permission.PermissionSupplier

//? if fabric {
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
//?} else {
/*import net.neoforged.neoforge.common.extensions.ICommonPacketListener
import net.neoforged.neoforge.network.registration.NetworkRegistry
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

    override fun canSee(player: McServerPlayer): Boolean =
        !McPlayerVisibilityCheckEvent.invoker.shouldHide(this, player)

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

        instance.connection.send(
            ClientboundSetActionBarTextPacket(component)
        )
    }

    override fun sendMessage(text: McTextComponent) {
        val json = minecraftServer.textConverter.convertToJson(this, text)
        val component = ComponentTextConverter.convertFromJson(json)

        instance.sendSystemMessage(component)
    }

    override fun sendPacket(channel: String, data: ByteArray) {
        val channelKey = ResourceLocation.tryParse(channel) ?: throw IllegalArgumentException("Invalid channel key")
        //? if fabric {
        val codec = ModChannelManager.getOrRegisterCodec(channelKey)
        ServerPlayNetworking.send(instance, ByteArrayPayload(codec.type, data))
        //?} else {
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
