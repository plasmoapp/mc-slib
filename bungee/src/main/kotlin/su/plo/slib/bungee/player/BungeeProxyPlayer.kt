package su.plo.slib.bungee.player

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.connection.InitialHandler
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.proxy.player.McProxyPlayer
import su.plo.slib.bungee.BungeeProxyLib
import su.plo.slib.bungee.connection.BungeeProxyServerConnection
import su.plo.slib.permission.PermissionSupplier
import java.util.*

class BungeeProxyPlayer(
    private val minecraftProxy: BungeeProxyLib,
    private val permissions: PermissionSupplier,
    private val instance: ProxiedPlayer
) : McProxyPlayer {

    override val language: String
        get() = instance.locale.toString()

    override val uuid: UUID
        get() = instance.uniqueId

    override val name: String
        get() = instance.name

    override val gameProfile: McGameProfile
        get() {
            val connection = instance.pendingConnection as InitialHandler
            val gameProfile = connection.loginProfile

            return McGameProfile(
                connection.uniqueId,
                connection.name,
                gameProfile?.properties?.map {
                    McGameProfile.Property(
                        it.name,
                        it.value,
                        it.signature
                    )
                } ?: emptyList()
            )
        }

    override val isOnline: Boolean
        get() = instance.isConnected

    override var server: BungeeProxyServerConnection? = null
        get() {
            val currentServer = instance.server
            if (currentServer == null) {
                field = null
                return field
            }

            if (field?.instance == currentServer) {
                return field
            }

            field = BungeeProxyServerConnection(minecraftProxy, currentServer)
            return field
        }

    private val audience: Audience
        get() = minecraftProxy.adventure.player(instance)

    override fun sendMessage(text: McTextComponent) {
        val json = minecraftProxy.textConverter.convertToJson(text)
        val component = GsonComponentSerializer.gson().deserialize(json)

        audience.sendMessage(component)
    }

    override fun sendActionBar(text: McTextComponent) {
        val json = minecraftProxy.textConverter.convertToJson(text)
        val component = GsonComponentSerializer.gson().deserialize(json)

        audience.sendActionBar(component)
    }

    override fun sendPacket(channel: String, data: ByteArray) =
        instance.sendData(channel, data)

    override fun kick(reason: McTextComponent) {
        val json = minecraftProxy.textConverter.convertToJson(reason)
        val component = GsonComponentSerializer.gson().deserialize(json)

        val textReason = LegacyComponentSerializer.legacySection().serialize(component)

        instance.disconnect(textReason)
    }

    override fun hasPermission(permission: String) =
        permissions.hasPermission(instance, permission)

    override fun getPermission(permission: String) =
        permissions.getPermission(instance, permission)

    @Suppress("UNCHECKED_CAST")
    override fun <T> getInstance(): T =
        instance as T
}
