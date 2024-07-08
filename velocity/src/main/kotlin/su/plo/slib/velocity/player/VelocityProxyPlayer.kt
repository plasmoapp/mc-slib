package su.plo.slib.velocity.player

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.api.proxy.player.McProxyPlayer
import su.plo.slib.permission.PermissionSupplier
import su.plo.slib.velocity.connection.VelocityProxyServerConnection
import su.plo.slib.velocity.extension.textConverter
import java.util.*

class VelocityProxyPlayer(
    private val minecraftProxy: McProxyLib,
    private val permissions: PermissionSupplier,
    private val instance: Player
) : McProxyPlayer {

    override val language: String
        get() = instance.playerSettings.locale.toString()

    override val uuid: UUID
        get() = instance.uniqueId

    override val name: String
        get() = instance.username

    override val gameProfile: McGameProfile
        get() {
            val gameProfile = instance.gameProfile

            return McGameProfile(
                gameProfile.id,
                gameProfile.name,
                gameProfile.properties.map {
                    McGameProfile.Property(it.name, it.value, it.signature)
                }
            )
        }

    override val isOnline: Boolean
        get() = instance.isActive

    override var server: VelocityProxyServerConnection? = null
        get() {
            val currentServer = instance.currentServer.orElse(null)
            if (currentServer == null) {
                field = null
                return field
            }

            if (field?.instance == currentServer) {
                return field
            }

            field = VelocityProxyServerConnection(minecraftProxy, currentServer)
            return field
        }

    override fun sendMessage(text: McTextComponent) {
        val json = minecraftProxy.textConverter.convertToJson(this, text)
        val component = GsonComponentSerializer.gson().deserialize(json)

        instance.sendMessage(component)
    }

    override fun sendActionBar(text: McTextComponent) {
        val json = minecraftProxy.textConverter.convertToJson(this, text)
        val component = GsonComponentSerializer.gson().deserialize(json)

        instance.sendActionBar(component)
    }

    override fun sendPacket(channel: String, data: ByteArray) {
        instance.sendPluginMessage(MinecraftChannelIdentifier.from(channel), data)
    }

    override fun kick(reason: McTextComponent) {
        val json = minecraftProxy.textConverter.convertToJson(this, reason)
        val component = GsonComponentSerializer.gson().deserialize(json)

        instance.disconnect(component)
    }

    override fun hasPermission(permission: String) =
        permissions.hasPermission(instance, permission)

    override fun getPermission(permission: String) =
        permissions.getPermission(instance, permission)

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T =
        instance as T
}
