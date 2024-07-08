package su.plo.slib.minestom.command

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minestom.server.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.chat.converter.ServerTextConverter
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.permission.PermissionTristate

class MinestomDefaultCommandSource(
    private val textConverter: ServerTextConverter<*>,
    private val source: CommandSender
) : McCommandSource {

    override fun sendMessage(text: McTextComponent) {
        val json = textConverter.convertToJson(this, text)
        val component = GsonComponentSerializer.gson().deserialize(json)

        source.sendMessage(component)
    }

    override fun sendActionBar(text: McTextComponent) {
        val json = textConverter.convertToJson(this, text)
        val component = GsonComponentSerializer.gson().deserialize(json)

        source.sendActionBar(component)
    }

    override val language: String
        get() = "en_us"

    override fun hasPermission(permission: String): Boolean = true

    override fun getPermission(permission: String): PermissionTristate = PermissionTristate.FALSE
}
