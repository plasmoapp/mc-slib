package su.plo.slib.paper.entity

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.event.player.McPlayerVisibilityCheckEvent
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.paper.PaperServerLib
import su.plo.slib.paper.util.extension.toAdventure
import su.plo.slib.permission.PermissionSupplier

class PaperServerPlayer(
    private val loader: JavaPlugin,
    minecraftServer: PaperServerLib,
    private val permissions: PermissionSupplier,
    player: Player
) : PaperServerEntity<Player>(minecraftServer, player), McServerPlayer {

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
        instance.sendMessage(text.toAdventure(minecraftServer, this))
    }

    override fun sendActionBar(text: McTextComponent) {
        instance.sendActionBar(text.toAdventure(minecraftServer, this))
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
        instance.kick(reason.toAdventure(minecraftServer, this))
    }

    override fun canSee(player: McServerPlayer): Boolean =
        !McPlayerVisibilityCheckEvent.invoker.shouldHide(this, player)
}
