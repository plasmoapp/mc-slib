package su.plo.slib.bungee.command

import net.md_5.bungee.api.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.bungee.extension.textConverter

class BungeeDefaultCommandSource(
    private val minecraftProxy: McProxyLib,
    private val source: CommandSender
) : McCommandSource {

    override val language: String
        get() = "en_us"

    override fun sendMessage(text: McTextComponent) {
        source.sendMessage(minecraftProxy.textConverter().convert(this, text))
    }

    override fun sendActionBar(text: McTextComponent) {
        sendMessage(text)
    }

    override fun hasPermission(permission: String) =
        source.hasPermission(permission)

    override fun getPermission(permission: String) =
        if (source.permissions.contains(permission)) {
            PermissionTristate.fromBoolean(source.hasPermission(permission))
        } else PermissionTristate.UNDEFINED
}
