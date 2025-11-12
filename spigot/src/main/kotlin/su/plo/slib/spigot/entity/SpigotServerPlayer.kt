package su.plo.slib.spigot.entity

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.permission.PermissionSupplier
import su.plo.slib.spigot.SpigotServerLib
import su.plo.slib.spigot.util.extension.sendActionBar
import su.plo.slib.spigot.util.extension.sendMessage

class SpigotServerPlayer(
    private val loader: JavaPlugin,
    minecraftServer: SpigotServerLib,
    private val permissions: PermissionSupplier,
    player: Player
) : SpigotServerEntity<Player>(minecraftServer, player), McServerPlayer {

    override val gameProfile: McGameProfile
        get() = minecraftServer.getGameProfile(instance.uniqueId) ?: throw IllegalStateException("Game profile not found")

    override val hasLabelScoreboard: Boolean
        get() = instance.scoreboard.getObjective(DisplaySlot.BELOW_NAME) != null

    override val isOnline: Boolean
        get() = instance.isOnline

    override val isSneaking: Boolean
        get() = instance.isSneaking

    override val isSpectator: Boolean
        get() = instance.gameMode == GameMode.SPECTATOR

    override val language: String
        get() = instance.locale

    override val name: String
        get() = instance.name

    override val registeredChannels: Collection<String>
        get() = instance.listeningPluginChannels

    override val spectatorTarget: McServerEntity? = null
        get() {
            if (instance.spectatorTarget == null) {
                return null
            } else if (field == null ||
                instance.spectatorTarget != field.getInstance()
            ) {
                return minecraftServer.getEntityByInstance(instance.spectatorTarget!!)
            }

            return field
        }

    override fun sendMessage(text: McTextComponent) {
        instance.sendMessage(minecraftServer, text)
    }

    override fun sendActionBar(text: McTextComponent) {
        instance.sendActionBar(minecraftServer, text)
    }

    override fun hasPermission(permission: String) =
        permissions.hasPermission(instance, permission)

    override fun getPermission(permission: String) =
        permissions.getPermission(instance, permission)

    override fun sendPacket(channel: String, data: ByteArray) {
        if (!isOnline || !loader.isEnabled) return
        instance.sendPluginMessage(loader, channel, data)
    }

    override fun kick(reason: McTextComponent) {
        val json = minecraftServer.textConverter.convertToJson(this, reason)
        val component = GsonComponentSerializer.gson().deserialize(json)

        val textReason = LegacyComponentSerializer.legacySection().serialize(component)

        instance.kickPlayer(textReason)
    }

    override fun canSee(player: McServerPlayer): Boolean {
        val serverPlayer = (player as SpigotServerPlayer).instance

        if (serverPlayer.gameMode == GameMode.SPECTATOR) {
            return instance.gameMode == GameMode.SPECTATOR && instance.canSee(serverPlayer)
        }

        return instance.canSee(serverPlayer)
    }
}
