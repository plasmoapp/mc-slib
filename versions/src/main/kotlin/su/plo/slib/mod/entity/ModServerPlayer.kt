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
import su.plo.slib.mod.extension.getObjectiveBelowName
import su.plo.slib.mod.extension.textConverter
import su.plo.slib.permission.PermissionSupplier

//#if FABRIC
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
//#else

//$$ import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket

//#if MC>=12002
//$$ import net.minecraftforge.network.NetworkDirection
//#endif

//#endif

//#if MC>=11701
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
//#else
//$$ import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket
//#endif

//#if MC<11900
//$$ import net.minecraft.Util
//#endif

class ModServerPlayer(
    private val minecraftServer: McServerLib,
    private val permissions: PermissionSupplier,
    player: ServerPlayer
) : ModServerEntity<ServerPlayer>(minecraftServer, player), McServerPlayer {

    override val gameProfile: McGameProfile
        get() = minecraftServer.getGameProfile(instance.uuid) ?: throw IllegalStateException("Game profile not found")

    override val name: String
        get() = instance.gameProfile.name

    override val isSpectator: Boolean
        get() = instance.isSpectator

    override val isSneaking: Boolean
        get() = instance.isDescending

    override val hasLabelScoreboard: Boolean
        get() = instance.scoreboard.getObjectiveBelowName() != null

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
        instance.connection.disconnect(minecraftServer.textConverter().convert(this, reason))
    }

    override fun sendActionBar(text: McTextComponent) {
        //#if MC>=11701
        instance.connection.send(
            ClientboundSetActionBarTextPacket(
                minecraftServer.textConverter().convert(this, text)
            )
        )
        //#else
        //$$ instance.connection.send(
        //$$     ClientboundSetTitlesPacket(
        //$$         ClientboundSetTitlesPacket.Type.ACTIONBAR,
        //$$         minecraftServer.textConverter().convert(this, text)
        //$$     )
        //$$ )
        //#endif
    }

    override fun sendMessage(text: McTextComponent) {
        //#if MC>=11900
        instance.sendSystemMessage(minecraftServer.textConverter().convert(this, text))
        //#else
        //$$ instance.sendMessage(minecraftServer.textConverter().convert(this, text), Util.NIL_UUID);
        //#endif
    }

    override fun sendPacket(channel: String, data: ByteArray) {
        val channelKey = ResourceLocation(channel)
        val buf = FriendlyByteBuf(Unpooled.wrappedBuffer(data))

        //#if FABRIC
        ServerPlayNetworking.send(instance, channelKey, buf)
        //#else
        //#if MC>=12002
        //$$ val packet = NetworkDirection.PLAY_TO_CLIENT
        //$$     .buildPacket<ClientboundCustomPayloadPacket>(buf, channelKey)
        //$$     .getThis()
        //#else
        //$$ val packet = ClientboundCustomPayloadPacket(channelKey, buf)
        //#endif
        //$$ instance.connection.send(packet)
        //#endif
    }

    fun addChannel(channel: String) =
        registeredChannels.add(channel)
}
