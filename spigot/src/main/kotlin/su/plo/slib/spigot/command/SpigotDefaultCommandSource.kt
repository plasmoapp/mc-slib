package su.plo.slib.spigot.command

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.spigot.SpigotServerLib

class SpigotDefaultCommandSource(
    private val minecraftServer: SpigotServerLib,
    private val source: CommandSender
) : McCommandSource {

    private val audience: Audience
        get() = minecraftServer.adventure.sender(source)

    override fun sendMessage(text: McTextComponent) {
        val json = minecraftServer.textConverter.convertToJson(text)
        val component = GsonComponentSerializer.gson().deserialize(json)

        audience.sendMessage(component)
    }

    override fun sendActionBar(text: McTextComponent) {
        val json = minecraftServer.textConverter.convertToJson(text)
        val component = GsonComponentSerializer.gson().deserialize(json)

        audience.sendActionBar(component)
    }

    override val language: String
        get() = "en_us"

    override fun hasPermission(permission: String): Boolean = true

    override fun getPermission(permission: String): PermissionTristate = PermissionTristate.FALSE
}
