package su.plo.slib.minestom.command

import net.minestom.server.command.CommandSender
import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.permission.PermissionTristate
import su.plo.slib.minestom.chat.BaseComponentTextConverter

class MinestomDefaultCommandSource(
        private val textConverter: BaseComponentTextConverter,
        private val source: CommandSender
) : McCommandSource {

    override fun sendMessage(text: McTextComponent) =
        source.sendMessage(textConverter.convert(this, text))

    override fun sendActionBar(text: McTextComponent) =
        source.sendMessage(textConverter.convert(this, text))

    override val language: String
        get() = "en_us"

    override fun hasPermission(permission: String): Boolean = true

    override fun getPermission(permission: String): PermissionTristate = PermissionTristate.FALSE
}
