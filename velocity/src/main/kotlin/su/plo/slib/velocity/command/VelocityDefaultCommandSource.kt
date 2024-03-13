package su.plo.slib.velocity.command

import com.velocitypowered.api.command.CommandSource
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
        source.sendMessage(textConverter.convert(this, text))
    }

    override fun sendActionBar(text: McTextComponent) {
        source.sendActionBar(textConverter.convert(this, text))
    }

    override fun hasPermission(permission: String) =
        source.hasPermission(permission)

    override fun getPermission(permission: String) =
        source.getPermissionValue(permission).toPermissionTristate()
}
