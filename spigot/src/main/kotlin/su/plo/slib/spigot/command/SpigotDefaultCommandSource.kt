package su.plo.slib.spigot.command

import org.bukkit.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.spigot.chat.BaseComponentTextConverter

class SpigotDefaultCommandSource(
    private val textConverter: BaseComponentTextConverter,
    private val source: CommandSender
) : McCommandSource {

    override fun sendMessage(text: McTextComponent) =
        source.spigot().sendMessage(textConverter.convert(this, text))

    override fun sendActionBar(text: McTextComponent) =
        source.spigot().sendMessage(textConverter.convert(this, text))

    override val language: String
        get() = "en_us"

    override fun hasPermission(permission: String): Boolean = true

    override fun getPermission(permission: String): PermissionTristate = PermissionTristate.FALSE
}
