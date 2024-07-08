package su.plo.slib.bungee.command

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.md_5.bungee.api.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.bungee.BungeeProxyLib

class BungeeDefaultCommandSource(
    private val minecraftProxy: BungeeProxyLib,
    private val source: CommandSender
) : McCommandSource {

    override val language: String
        get() = "en_us"

    private val audience: Audience
        get() = minecraftProxy.adventure.sender(source)

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

    override fun hasPermission(permission: String) =
        source.hasPermission(permission)

    override fun getPermission(permission: String) =
        if (source.permissions.contains(permission)) {
            PermissionTristate.fromBoolean(source.hasPermission(permission))
        } else PermissionTristate.UNDEFINED
}
