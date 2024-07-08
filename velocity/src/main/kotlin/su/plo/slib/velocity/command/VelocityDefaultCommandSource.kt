package su.plo.slib.velocity.command

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.chat.AdventureComponentTextConverter
import su.plo.slib.velocity.extension.toPermissionTristate

class VelocityDefaultCommandSource(
    private val textConverter: AdventureComponentTextConverter,
    private val source: CommandSource
) : McCommandSource {

    override val language: String
        get() = "en_us"

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

    override fun hasPermission(permission: String) =
        source.hasPermission(permission)

    override fun getPermission(permission: String) =
        source.getPermissionValue(permission).toPermissionTristate()
}
