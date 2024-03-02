package su.plo.slib.minestom.entity

import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import su.plo.slib.api.server.McServerLib
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.slib.api.server.entity.player.McServerPlayer
import su.plo.slib.permission.PermissionSupplier
import su.plo.slib.minestom.extension.textConverter

class MinestomServerPlayer(
    minecraftServer: McServerLib,
    private val permissions: PermissionSupplier,
    player: Player
) : MinestomServerEntity<Player>(minecraftServer, player), McServerPlayer {

    override val gameProfile: McGameProfile
        get() = minecraftServer.getGameProfile(instance.uuid) ?: throw IllegalStateException("Game profile not found")

    // While Minestom does allow us to set the below name tag, we can't actually check if its set.
    // This hack works around this issue using reflection.
    override val hasLabelScoreboard: Boolean
        get() = Player::class.java.getDeclaredField("belowNameTag").let {
            it.isAccessible = true
            it.get(instance) != null
        }

    override val isOnline: Boolean
        get() = instance.isOnline

    override val isSneaking: Boolean
        get() = instance.isSneaking

    override val isSpectator: Boolean
        get() = instance.gameMode == GameMode.SPECTATOR

    override val language: String
        get() = instance.locale?.language ?: "en_us"

    override val name: String
        get() = instance.username

    override val registeredChannels: MutableCollection<String> = mutableSetOf()

    override val spectatorTarget: McServerEntity? = null

    override fun sendMessage(text: McTextComponent) =
        instance.sendMessage(minecraftServer.textConverter().convert(this, text))

    override fun sendActionBar(text: McTextComponent) =
        instance.sendActionBar(minecraftServer.textConverter().convert(this, text))


    override fun hasPermission(permission: String) =
        permissions.hasPermission(instance, permission)

    override fun getPermission(permission: String) =
        permissions.getPermission(instance, permission)

    override fun sendPacket(channel: String, data: ByteArray) {
        if (!isOnline) return
        instance.sendPluginMessage(channel, data)
    }

    override fun kick(reason: McTextComponent) {
        instance.kick(minecraftServer.textConverter().convert(this, reason))
    }

    override fun canSee(player: McServerPlayer): Boolean {
        val serverPlayer = (player as MinestomServerPlayer).instance

        if (serverPlayer.gameMode == GameMode.SPECTATOR) {
            return instance.gameMode == GameMode.SPECTATOR
        }

        // Minestom does not have invisibility settings like Paper
        return true
    }
}
