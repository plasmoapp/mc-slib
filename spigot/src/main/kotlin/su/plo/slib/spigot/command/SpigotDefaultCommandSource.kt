package su.plo.slib.spigot.command

import org.bukkit.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.spigot.SpigotServerLib
import su.plo.slib.spigot.util.extension.sendActionBar
import su.plo.slib.spigot.util.extension.sendMessage

class SpigotDefaultCommandSource(
    private val minecraftServer: SpigotServerLib,
    private val source: CommandSender
) : McCommandSource {

    override fun sendMessage(text: McTextComponent) {
        source.sendMessage(minecraftServer, text)
    }

    override fun sendActionBar(text: McTextComponent) {
        source.sendActionBar(minecraftServer, text)
    }

    override val language: String
        get() = "en_us"

    override fun hasPermission(permission: String): Boolean = true

    override fun getPermission(permission: String): PermissionTristate = PermissionTristate.FALSE
}
