package su.plo.slib.mod.command

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.slib.api.command.McCommandSource
import su.plo.slib.api.permission.PermissionTristate

object ModUnboundCommandSource : McCommandSource {
    override val language: String
        get() = "en_us"

    override fun sendMessage(text: McTextComponent) = Unit

    override fun sendActionBar(text: McTextComponent) = Unit

    override fun hasPermission(permission: String) = true

    override fun getPermission(permission: String) = PermissionTristate.UNDEFINED
}
